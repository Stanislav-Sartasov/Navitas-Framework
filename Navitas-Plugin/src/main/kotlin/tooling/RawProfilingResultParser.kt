package tooling

import data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.Files.readAllBytes
import java.nio.file.Paths

object RawProfilingResultParser {

    fun parse(directory: String, fileName: String): RawProfilingResult {
        val result = RawProfilingResult()
        val json = String(readAllBytes(Paths.get(directory, fileName)))
        val obj = JSONObject(json)
        JSONObject()
        val tests = obj.getJSONArray("tests")

        for (i in 0 until tests.length()) {
            val test = tests.getJSONObject(i)
            val testName = test.getString("testName")
            val testLogs = test.getJSONArray("logs")
            val rawMethodLogs = mutableListOf<RawMethodLog>()

            for (j in 0 until testLogs.length()) {
                val log = testLogs.getJSONObject(j)
                val methodInfo = parseMethodInfo(log.getJSONObject("header"))
                val components = log.getJSONArray("body")

                lateinit var cpuTimeInStates: CpuTimeInStates
                var brightnessLevel = 0

                for (k in 0 until components.length()) {
                    val component = components.getJSONObject(k)
                    when (component.getString("component")) {
                        "cpuTimeInStates" -> cpuTimeInStates = parseCpuTimeInStates(component.getJSONArray("details"))
                        "brightness" -> brightnessLevel = component.getInt("details")
                    }
                }

                rawMethodLogs.add(RawMethodLog(methodInfo, cpuTimeInStates, brightnessLevel))
            }

            result.addTestResults(testName, rawMethodLogs)
        }

        return result
    }

    private fun parseMethodInfo(obj: JSONObject): MethodInfo {
        val processID = obj.getInt("processID")
        val threadID = obj.getInt("threadID")
        val methodName = obj.getString("methodName")
        val timestamp = obj.getLong("timestamp")
        val isEntry = obj.getBoolean("isEntry")
        return MethodInfo(methodName, timestamp, processID, threadID, isEntry)
    }

    private fun parseCpuTimeInStates(array: JSONArray): CpuTimeInStates {
        val coreTimeInStates = mutableListOf<List<CoreTimeAtFrequency>>()

        for (i in 0 until array.length()) {
            val core = array.getJSONObject(i)
            val coreNumber = core.getInt("kernel")
            val coreDetails = core.getJSONArray("details")
            val currentCoreTimeInStates = mutableListOf<CoreTimeAtFrequency>()

            for (j in 0 until coreDetails.length()) {
                val details = coreDetails.getJSONObject(j)
                val frequency = details.getLong("frequency")
                val timestamp = details.getLong("timestamp")
                currentCoreTimeInStates.add(CoreTimeAtFrequency(frequency, timestamp))
            }

            coreTimeInStates.add(coreNumber, currentCoreTimeInStates)
        }

        return CpuTimeInStates(coreTimeInStates)
    }
}