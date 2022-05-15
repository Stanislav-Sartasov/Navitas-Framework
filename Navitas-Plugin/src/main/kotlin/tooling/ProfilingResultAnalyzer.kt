package tooling

import data.model.CpuInfo
import domain.model.PowerProfile
import domain.model.CpuMethodEnergyConsumption
import data.model.ProfilingResult
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
            val energy = testResult.second.uidInfo.first().energy
            val memory = testResult.second.info.first().memory
            val packets = testResult.second.info.first().packets
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
                                entryLog = if (logDeque.isNotEmpty()) { logDeque.pollLast() } else log,
                                exitLog = log,
                            nestedMethods = if (nestedMethodsDeque.isNotEmpty()) { nestedMethodsDeque.pollLast() } else mutableListOf(),
                            profile = powerProfile
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

            result.add(DetailedTestEnergyConsumption(testName, cpu, testResult.second.wifiInfo?.last()?.wifiEnergyConsumption,
                testResult.second.bluetoothInfo?.last()?.bluetoothEnergyConsumption, memory, packets, energy))
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
}

fun processNode(node: CpuMethodEnergyConsumption, lvl: Int = 0) {
    val offset = " ".repeat(lvl)
    println(offset + node.methodName + " " + node.cpuEnergy + " " + node.startTimestamp + ".." + node.endTimestamp)
    for (child in node.nestedMethods) {
        processNode(child, lvl + 4)
    }
}

fun main() {
    val profile = PowerProfileManager.getDefaultProfile()
    val raw = ProfilerResultParser.parse("/home/rinatisk/jsons/wifi/", "logs6.json")
    val result = ProfilingResultAnalyzer.analyze(raw, profile)
    for (test in result) {
        println(test.testName)
        println(test.wifiEnergyConsumption?.wifi)
        println(test.wifiEnergyConsumption?.common)
        for (info in test.cpuEnergyConsumption.testDetails) {
            println("Process ${info.key.first}, Thread ${info.key.second}")
            for (method in info.value) {
                processNode(method)
            }
            println("----------------------------------------------------")
        }
    }
}