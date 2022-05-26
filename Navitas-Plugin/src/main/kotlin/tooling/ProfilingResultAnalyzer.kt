package tooling

import data.model.CpuInfo
import data.model.GpuInfo
import data.model.MaliGPU
import data.model.ProfilingResult
import data.model.components.DisplayEnergyConsumption
import data.model.components.GpuEnergyConsumption
import domain.model.CpuEnergyConsumption
import domain.model.CpuMethodEnergyConsumption
import domain.model.DetailedTestEnergyConsumption
import domain.model.PowerProfile
import extensions.roundWithAccuracy
import org.jetbrains.kotlin.idea.actions.internal.benchmark.LocalCompletionBenchmarkAction
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.*

object ProfilingResultAnalyzer {
    fun analyze(profilingResult: ProfilingResult, powerProfile: PowerProfile): List<DetailedTestEnergyConsumption> {
        val result = mutableListOf<DetailedTestEnergyConsumption>()

        for (testResult in profilingResult.getTestResults()) {

            val testName = testResult.first
            val testDetails = mutableMapOf<Pair<Int, Int>, List<CpuMethodEnergyConsumption>>()
            var testEnergy = 0F

            val logs = testResult.second.cpuInfo
                    .sortedBy { log -> log.methodInfo.timestamp }
                    .groupBy { log -> log.methodInfo.processID to log.methodInfo.threadID }

            for (logGroup in logs) {
                val externalMethods = mutableListOf<CpuMethodEnergyConsumption>()
                val logDeque = ArrayDeque<CpuInfo>()
                val nestedMethodsDeque = ArrayDeque<MutableList<CpuMethodEnergyConsumption>>()

                for (log in logGroup.value) {
                    if (log.methodInfo.isEntry) {
                        logDeque.addLast(log)
                        nestedMethodsDeque.addLast(mutableListOf())
                    } else {
                        val methodDetails = analyzeMethodLogs(
                                logDeque.pollLast(),
                                log,
                                nestedMethodsDeque.pollLast(),
                                powerProfile
                        )
                        if (logDeque.isEmpty()) {
                            externalMethods.add(methodDetails)
                            testEnergy += methodDetails.cpuEnergy
                        } else {
                            nestedMethodsDeque.peekLast().add(methodDetails)
                        }
                    }
                }

                testDetails[logGroup.key] = externalMethods
            }
            val cpu = CpuEnergyConsumption(testEnergy, testDetails)
            val gpu = analyzeGpuEC(testResult.second.gpuInfo)
            val display = analyzeDisplayEC(testResult.second.cpuInfo?.last()?.brightnessLevel)
            result.add(DetailedTestEnergyConsumption(testName, cpu, testResult.second.wifiInfo?.last()?.wifiEnergyConsumption,
                testResult.second.bluetoothInfo?.last()?.bluetoothEnergyConsumption, gpu, display))
        }

        return result
    }

    private fun analyzeMethodLogs(
        entryLog: CpuInfo,
        exitLog: CpuInfo,
        nestedMethods: List<CpuMethodEnergyConsumption>,
        profile: PowerProfile
    ): CpuMethodEnergyConsumption {
        val methodName = entryLog.methodInfo.methodName
        val startTimestamp = entryLog.methodInfo.timestamp
        val endTimestamp = exitLog.methodInfo.timestamp
        var cpuEnergy = 0F

        val entryCpuLog = entryLog.cpuTimeInStates.coreTimeInStates
        val exitCpuLog = exitLog.cpuTimeInStates.coreTimeInStates

        for (i in entryCpuLog.indices) {
            for (j in entryCpuLog[i].indices) {
                val freq = entryCpuLog[i][j].frequency
                val time1 = entryCpuLog[i][j].timestamp
                val time2 = exitCpuLog[i][j].timestamp
                cpuEnergy += ((time2 - time1) / 3600f) * profile.getPowerAtSpeed(i, freq)
            }
        }
        cpuEnergy = cpuEnergy.roundWithAccuracy(2)

        return CpuMethodEnergyConsumption(
                methodName,
                startTimestamp,
                endTimestamp,
                cpuEnergy,
                nestedMethods
        )
    }

    private fun analyzeGpuEC(gpuInfoList: List<GpuInfo>?): GpuEnergyConsumption? {
        if (gpuInfoList == null) return null

        var result = 0F
        for (element in gpuInfoList) {
            val util = element.gpuEnergyConsumption.gpu.toInt()
            val coef = when(util) {
                in 0..158 -> MaliGPU().coef[0]
                in 159..217 -> MaliGPU().coef[1]
                in 218 .. 230 -> MaliGPU().coef[2]
                else -> MaliGPU().coef[3]
            }

            result += (coef.first * util + coef.second) / MaliGPU().volt
        }

        return GpuEnergyConsumption(0F, result, null)
    }

    private fun analyzeDisplayEC(brL: Int?): DisplayEnergyConsumption? {
        if (brL == null) return null

        var pixelsConsumption = 0.0

        val imagePath = "/home/debian/screenshot.png"
        val rgb = IntArray(3)
        val file = File(imagePath)

        val bi: BufferedImage? = ImageIO.read(file)

        if (bi == null) return null
        val width = bi.width
        val height = bi.height
        val minx = bi.minX
        val miny = bi.minY

        val yb = 0.058F
        val yr = { x: Int -> 0.0091208 * exp(0.0083006*x) }
        val yg = { x: Int -> 0.0067118 * exp(0.0097693*x) }
        val ybl = { x: Int -> 0.0125597 * exp(0.0094219*x) }

        val yw = { x: Int -> 0.0109622 * exp(0.0129088*x) }
        val yy = { x: Int -> 0.0117486 * exp(0.0104776*x) }
        val yp = { x: Int -> 0.0111043 * exp(0.0119207*x) }
        val yc = { x: Int -> 0.0154045 * exp(0.0104654*x) }

        val ydiffW = { x: Int -> yw(x) - yr(x) - yg(x) - ybl(x) }
        val ydiffY = { x: Int -> yy(x) - yr(x) - yg(x) }
        val ydiffP = { x: Int -> yp(x) - yr(x) - ybl(x) }
        val ydiffC = { x: Int -> yc(x) - yg(x) - ybl(x) }

        val yQW = { x: Int -> yw(x)/(yw(x) - ydiffW(x)) }
        val yQY = { x: Int -> yy(x)/(yy(x) - ydiffY(x)) }
        val yQP = { x: Int -> yp(x)/(yp(x) - ydiffP(x)) }
        val yQC = { x: Int -> yc(x)/(yc(x) - ydiffC(x)) }

        val yQmid = { x: Int -> (3*yQW(x) + yQY(x) + yQP(x) + yQC(x))/6 }

        val yGk0 = { x: Int -> 3.54727978707 * 10.0.pow(-13) * x*(x - 68)*(x - 136)*(x - 204)*(x - 255)
            - 4.27307683393 * 10.0.pow(-13) * x*(x - 34)*(x - 136)*(x - 204)*(x - 255)
            + 6.96866983007 * 10.0.pow(-13) * x*(x - 34)*(x - 68)*(x - 204)*(x - 255)
            - 5.16285666076 * 10.0.pow(-13) * x*(x - 34)*(x - 68)*(x - 136)*(x - 255)
            + 1.71398554112 * 10.0.pow(-13) * x*(x - 34)*(x - 68)*(x - 136)*(x - 204) }
        val yGk42 = { x: Int -> 3.54727978707 * 10.0.pow(-13) * x*(x - 68)*(x - 136)*(x - 204)*(x - 255)
            - 5.67681601750 * 10.0.pow(-13) * x*(x - 34)*(x - 136)*(x - 204)*(x - 255)
            + 1.03868441054 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 204)*(x - 255)
            - 8.68418091624 * 10.0.pow(-13) * x*(x - 34)*(x - 68)*(x - 136)*(x - 255)
            + 2.94760572354 * 10.0.pow(-13) * x*(x - 34)*(x - 68)*(x - 136)*(x - 204) }
        val yGk84 = { x: Int -> 3.54727978707 * 10.0.pow(-13) * x*(x - 68)*(x - 136)*(x - 204)*(x - 255)
            - 7.54169450939 * 10.0.pow(-13) * x*(x - 34)*(x - 136)*(x - 204)*(x - 255)
            + 1.54816533284 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 204)*(x - 255)
            - 1.46072229275 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 136)*(x - 255)
            + 5.06910898197 * 10.0.pow(-13) * x*(x - 34)*(x - 68)*(x - 136)*(x - 204) }
        val yGk126 = { x: Int -> 3.54727978707 * 10.0.pow(-13) * x*(x - 68)*(x - 136)*(x - 204)*(x - 255)
            - 1.00192001815 * 10.0.pow(-12) * x*(x - 34)*(x - 136)*(x - 204)*(x - 255)
            + 2.30754969795 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 204)*(x - 255)
            - 2.45700732987 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 136)*(x - 255)
            + 8.71753832811 * 10.0.pow(-13) * x*(x - 34)*(x - 68)*(x - 136)*(x - 204) }
        val yGk168 = { x: Int -> 3.54727978707 * 10.0.pow(-13) * x*(x - 68)*(x - 136)*(x - 204)*(x - 255)
            - 1.33105858574 * 10.0.pow(-12) * x*(x - 34)*(x - 136)*(x - 204)*(x - 255)
            + 3.43941664082 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 204)*(x - 255)
            - 4.13280816559 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 136)*(x - 255)
            + 1.49918801849 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 136)*(x - 204) }
        val yGk210 = { x: Int -> 3.54727978707 * 10.0.pow(-13) * x*(x - 68)*(x - 136)*(x - 204)*(x - 255)
            - 1.76832174881 * 10.0.pow(-12) * x*(x - 34)*(x - 136)*(x - 204)*(x - 255)
            + 5.12647109603 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 204)*(x - 255)
            - 6.95158826985 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 136)*(x - 255)
            + 2.57821030456 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 136)*(x - 204) }
        val yGk255 = { x: Int -> 3.54727978707 * 10.0.pow(-13) * x*(x - 68)*(x - 136)*(x - 204)*(x - 255)
            - 2.39738138243 * 10.0.pow(-12) * x*(x - 34)*(x - 136)*(x - 204)*(x - 255)
            + 7.86200323810 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 204)*(x - 255)
            - 1.21354023235 * 10.0.pow(-11) * x*(x - 34)*(x - 68)*(x - 136)*(x - 255)
            + 4.60892146870 * 10.0.pow(-12) * x*(x - 34)*(x - 68)*(x - 136)*(x - 204) }

        val yGki0 = { x: Int -> yGk0(x)/yw(0) }
        val yGki42 = { x: Int -> yGk42(x)/yw(42) }
        val yGki84 = { x: Int -> yGk84(x)/yw(84) }
        val yGki126 = { x: Int -> yGk126(x)/yw(126) }
        val yGki168 = { x: Int -> yGk168(x)/yw(168) }
        val yGki210 = { x: Int -> yGk210(x)/yw(210) }
        val yGki255 = { x: Int -> yGk255(x)/yw(255) }

        val yGki = { i: Int, x: Int ->
            when (i) {
                in 0..20 -> yGki0(x)
                in 21..63 -> yGki42(x)
                in 64..105 -> yGki84(x)
                in 106..147 -> yGki126(x)
                in 148..189 -> yGki168(x)
                in 190..233 -> yGki210(x)
                else -> yGki255(x)
            }
        }


        for (i in minx until width) {
            for (j in miny until height) {
                val pixel = bi.getRGB(i, j) // The following three lines of code convert a number to an RGB number

                rgb[0] = pixel and 0xff0000 shr 16
                rgb[1] = pixel and 0xff00 shr 8
                rgb[2] = pixel and 0xff

                pixelsConsumption += ( yQmid(brL)*(yGki(brL, rgb[0])*yr(brL) + yGki(brL, rgb[1])*yg(brL)
                        + yGki(brL, rgb[2])*ybl(brL)) ) / 921.6
            }
        }

        val result = pixelsConsumption.toFloat() + yb * 1000

        return DisplayEnergyConsumption(0F, result, null)
    }
}

// ATTENTION: only for debug
//fun processNode(node: CpuMethodEnergyConsumption, lvl: Int = 0) {
//    val offset = " ".repeat(lvl)
//    println(offset + node.methodName + " " + node.cpuEnergy + " " + node.startTimestamp + ".." + node.endTimestamp)
//    for (child in node.nestedMethods) {
//        processNode(child, lvl + 4)
//    }
//}
//
//// ATTENTION: only for debug
//fun main() {
//    val profile = PowerProfileManager.getDefaultProfile()
//    val raw = ProfilerResultParser.parse("C:/Users/EG/Documents/Navitas-Framework/UI-Testing-Samples/app/profileOutput/", "logs.json")
//    val result = ProfilingResultAnalyzer.analyze(raw, profile)
//    for (test in result) {
//        println(test.testName)
//        for (info in test.cpuEnergyConsumption.testDetails) {
//            println("Process ${info.key.first}, Thread ${info.key.second}")
//            for (method in info.value) {
//                processNode(method)
//            }
//            println("----------------------------------------------------")
//        }
//    }
//}