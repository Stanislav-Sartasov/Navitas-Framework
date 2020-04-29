package tooling

import data.model.MethodDetails
import data.model.RawMethodLog
import data.model.RawProfilingResult
import domain.model.DetailedTestEnergyConsumption
import java.util.*

object RawProfilingResultAnalyzer {

    fun analyze(raw: RawProfilingResult): List<DetailedTestEnergyConsumption> {
        val result = mutableListOf<DetailedTestEnergyConsumption>()

        for (testResult in raw.getTestResults()) {
            val testName = testResult.first
            val testDetails = mutableMapOf<Pair<Int, Int>, List<MethodDetails>>()
            var testEnergy = 0F

            val logs = testResult.second
                    .sortedBy { log -> log.methodInfo.timestamp }
                    .groupBy { log -> log.methodInfo.processID to log.methodInfo.threadID }

            for (logGroup in logs) {
                val externalMethods = mutableListOf<MethodDetails>()
                val logDeque = ArrayDeque<RawMethodLog>()
                val nestedMethodsDeque = ArrayDeque<MutableList<MethodDetails>>()

                for (log in logGroup.value) {
                    if (log.methodInfo.isEntry) {
                        logDeque.addLast(log)
                        nestedMethodsDeque.addLast(mutableListOf())
                    } else {
                        val methodDetails = analyzeMethodLogs(logDeque.pollLast(), log, nestedMethodsDeque.pollLast())
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

            result.add(DetailedTestEnergyConsumption(testName, testEnergy, testDetails))
        }

        return result
    }

    private fun analyzeMethodLogs(entryLog: RawMethodLog, exitLog: RawMethodLog, nestedMethods: List<MethodDetails>): MethodDetails {
        val methodName = entryLog.methodInfo.methodName
        val startTimestamp = entryLog.methodInfo.timestamp
        val endTimestamp = exitLog.methodInfo.timestamp
        var cpuEnergy = 0F

        val entryCpuLog = entryLog.cpuTimeInStates.coreTimeInStates
        val exitCpuLog = exitLog.cpuTimeInStates.coreTimeInStates

        for (i in entryCpuLog.indices) {
            for (j in entryCpuLog[i].indices) {
                val time1 = entryCpuLog[i][j].timestamp
                val time2 = exitCpuLog[i][j].timestamp
                cpuEnergy += (time2 - time1) * 0.1F   // TODO: use EnergyProfile's constants
            }
        }

        return MethodDetails(
                methodName,
                startTimestamp,
                endTimestamp,
                cpuEnergy,
                nestedMethods
        )
    }
}

// ATTENTION: only for debug
fun processNode(node: MethodDetails, lvl: Int = 0) {
    val offset = " ".repeat(lvl)
    println(offset + node.methodName + " " + node.cpuEnergy + " " + node.startTimestamp + ".." + node.endTimestamp)
    for (child in node.nestedMethods) {
        processNode(child, lvl + 4)
    }
}

// ATTENTION: only for debug
fun main() {
    val raw = RawProfilingResultParser.parse("/home/vladislav/Workspace/SPBU/Navitas-Framework/UI-Testing-Samples/app/profileOutput/", "logs.json")
    val result = RawProfilingResultAnalyzer.analyze(raw)
    for (test in result) {
        println(test.testName)
        for (info in test.testDetails) {
            println("Process ${info.key.first}, Thread ${info.key.second}")
            for (method in info.value) {
                processNode(method)
            }
            println("----------------------------------------------------")
        }
    }
}