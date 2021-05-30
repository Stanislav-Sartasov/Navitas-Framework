package tooling

import data.model.*
import data.model.components.*
import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.Files.readAllBytes
import java.nio.file.Paths

object ProfilingResultParser {
    fun parse(directory: String, fileName: String): ProfilingResult {
        val result = ProfilingResult()
        val json = String(readAllBytes(Paths.get(directory, fileName)))
        val obj = JSONObject(json)
        val tests = obj.getJSONArray("tests")

        for (i in 0 until tests.length()) {
            val test = tests.getJSONObject(i)
            val testName = test.getString("testName")
            val testLogs = test.getJSONObject("logs")

            val cpuInfo = mutableListOf<CpuInfo>()
            val wifiInfo = mutableListOf<WifiInfo>()
            val bluetoothInfo = mutableListOf<BluetoothInfo>()

            for(component in testLogs.keys()) {
                if(component != null) {
                    when(component) {
                        "wifi" -> {
                            val wifiArray = testLogs.getJSONArray("wifi")

                            for(j in 0 until wifiArray.length()) {
                                val wifiLog = wifiArray.getJSONObject(j)

                                wifiInfo.add(parseWifiInfo(wifiLog))
                            }
                        }
                        "bluetooth" -> {
                            val bluetoothArray = testLogs.getJSONArray("bluetooth")

                            for(j in 0 until bluetoothArray.length()) {
                                val bluetoothLog = bluetoothArray.getJSONObject(j)

                                bluetoothInfo.add(parseBluetoothInfo(bluetoothLog))
                            }
                        }
                        "cpu" -> {
                            val cpuLogs = testLogs.getJSONArray("cpu")
                            for (j in 0 until cpuLogs.length()) {
                                val methodLog = cpuLogs.getJSONObject(j)

                                val methodInfo = parseMethodInfo(methodLog.getJSONObject("header"))
                                lateinit var cpuTimeInStates : CpuTimeInStates
                                var brightnessLevel = 0

                                val methodCpuInfo = methodLog.getJSONArray("body")
                                for (k in 0 until methodCpuInfo.length()) {
                                    val info = methodCpuInfo.getJSONObject(k)
                                    when (info?.getString("component")) {
                                        "cpuTimeInStates" -> cpuTimeInStates = parseCpuTimeInStates(info.getJSONArray("details"))
                                        "brightness" -> brightnessLevel = info.getInt("details")
                                    }
                                }

                                cpuInfo.add(CpuInfo(methodInfo, cpuTimeInStates, brightnessLevel))
                            }
                        }
                    }
                }
            }

            val testLog = ProfilingTestLog(cpuInfo,
                if(wifiInfo.size != 0) wifiInfo else null,
                if(bluetoothInfo.size != 0) bluetoothInfo else null
            )

            result.addTestResults(testName, testLog)
        }

        return result
    }

    private fun parseTestInfo(obj: JSONObject): TestInfo {
        val frequency = obj.getFloat("frequency")
        val timestamp = obj.getLong("timestamp")

        return TestInfo(frequency, timestamp)
    }

    private fun parseWifiInfo(obj: JSONObject): WifiInfo {
        val testInfo = parseTestInfo(obj.getJSONObject("header"))

        var common = Float.NaN
        var wifi = Float.NaN
        val external = mutableListOf<ComponentEnergyPair>()

        val testDetails = obj.getJSONObject("body")
        for(component in testDetails.keys()) {
            if(component != null)
            {
                when (component) {
                    "common" -> {
                        common = testDetails.getFloat(component)
                    }
                    "wifi" -> {
                        wifi = testDetails.getFloat(component)
                    }
                    else -> {
                        val energy = testDetails.getFloat(component)
                        external.add(ComponentEnergyPair(component, energy))
                    }
                }
            }
        }

        if(external.isEmpty())
        {
            return WifiInfo(testInfo, WifiEnergyConsumption(common, wifi, null))
        }

        return WifiInfo(testInfo, WifiEnergyConsumption(common, wifi, external))
    }

    private fun parseBluetoothInfo(obj: JSONObject): BluetoothInfo {
        val testInfo = parseTestInfo(obj.getJSONObject("header"))

        var common = Float.NaN
        var bluetooth = Float.NaN
        val external = mutableListOf<ComponentEnergyPair>()

        val testDetails = obj.getJSONObject("body")
        for(component in testDetails.keys()) {
            if(component != null)
            {
                when (component) {
                    "common" -> {
                        common = testDetails.getFloat(component)
                    }
                    "bluetooth", "bt", "usage" -> {
                        bluetooth = testDetails.getFloat(component)
                    }
                    else -> {
                        val energy = testDetails.getFloat(component)
                        external.add(ComponentEnergyPair(component, energy))
                    }
                }
            }
        }

        if(external.isEmpty())
        {
            return BluetoothInfo(testInfo, BluetoothEnergyConsumption(common, bluetooth, null))
        }

        return BluetoothInfo(testInfo, BluetoothEnergyConsumption(common, bluetooth, external))
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