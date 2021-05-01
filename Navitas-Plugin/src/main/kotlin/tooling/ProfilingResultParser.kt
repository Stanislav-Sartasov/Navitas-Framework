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
        JSONObject()
        val tests = obj.getJSONArray("tests")

        for (i in 0 until tests.length()) {
            val test = tests.getJSONObject(i)
            val testName = test.getString("testName")
            val testLogs = test.getJSONObject("logs")

            val cpuInfo = mutableListOf<CpuInfo>()
            var wifiEnergyConsumption : WifiEnergyConsumption? = null
            var bluetoothEnergyConsumption : BluetoothEnergyConsumption? = null
            for(component in testLogs.keys())
            {
                if(component != null)
                {
                    when(component)
                    {
                        "wifi" -> {
                            val wifiLogs = testLogs.getJSONObject("wifi")
                            wifiEnergyConsumption = parseWifiEnergyConsumption(wifiLogs)
                        }
                        "bluetooth" -> {
                            val bluetoothLogs = testLogs.getJSONObject("bluetooth")
                            bluetoothEnergyConsumption = parseBluetoothEnergyConsumption(bluetoothLogs)
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

            val testLog = ProfilingTestLog(cpuInfo, wifiEnergyConsumption, bluetoothEnergyConsumption)

            result.addTestResults(testName, testLog)
        }

        return result
    }

    private fun parseWifiEnergyConsumption(obj: JSONObject): WifiEnergyConsumption {
        var common = 0f
        var wifi = 0f
        val external = mutableListOf<ComponentEnergyPair>()

        for(component in obj.keys()) {
            if(component != null)
            {
                when (component) {
                    "common" -> {
                        common = obj.getFloat(component)
                    }
                    "wifi" -> {
                        wifi = obj.getFloat(component)
                    }
                    else -> {
                        val energy = obj.getFloat(component)
                        external.add(ComponentEnergyPair(component, energy))
                    }
                }
            }
        }

        if(external.isEmpty())
        {
            return WifiEnergyConsumption(common, wifi, null)
        }

        return WifiEnergyConsumption(common, wifi, external)
    }

    private fun parseBluetoothEnergyConsumption(obj: JSONObject): BluetoothEnergyConsumption {
        var common = 0f
        var bluetooth = 0f
        val external = mutableListOf<ComponentEnergyPair>()

        for(component in obj.keys()) {
            if(component != null)
            {
                when (component) {
                    "common" -> {
                        common = obj.getFloat(component)
                    }
                    "bluetooth", "bt", "usage" -> {
                        bluetooth = obj.getFloat(component)
                    }
                    else -> {
                        val energy = obj.getFloat(component)
                        external.add(ComponentEnergyPair(component, energy))
                    }
                }
            }
        }

        if(external.isEmpty())
        {
            return BluetoothEnergyConsumption(common, bluetooth, null)
        }

        return BluetoothEnergyConsumption(common, bluetooth, external)
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