package tooling

import data.model.CpuInfo
import data.model.GpuInfo
import data.model.MaliGPU
import domain.model.PowerProfile
import domain.model.CpuMethodEnergyConsumption
import data.model.ProfilingResult
import data.model.components.GpuEnergyConsumption
import domain.model.CpuEnergyConsumption
import domain.model.DetailedTestEnergyConsumption
import extensions.roundWithAccuracy
import org.jetbrains.kotlin.idea.internal.makeBackup.random
import java.util.*

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
            result.add(DetailedTestEnergyConsumption(testName, cpu, testResult.second.wifiInfo?.last()?.wifiEnergyConsumption,
                testResult.second.bluetoothInfo?.last()?.bluetoothEnergyConsumption, gpu))
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