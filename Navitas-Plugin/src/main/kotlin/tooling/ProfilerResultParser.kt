package tooling

import data.model.*
import data.model.components.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Files.readAllBytes
import java.nio.file.Paths

fun main() {

    //!!This is cli api for analyzer to force check without plugin!!

    //  val sum = File("/home/rinatisk/jsons/usb/sum.txt")
        // val fileWriter = FileWriter("/home/rinatisk/jsons/usb/sum.txt", true)
    for (i in 1..1) {
        val result = ProfilerResultParser.parse("/home/rinatisk/StudioProjects/Navitas-Framework/NaviTests/navi_test/profilingOutput/", "logs.json")
        val defaultProfile = PowerProfileManager.getDefaultProfile()
      //  println(result.getTestResults())
        val analyzeResult = ProfilingResultAnalyzer.analyze(result, defaultProfile)
    //    println(i)
        var resultList = mutableListOf<Float>()
        analyzeResult.forEach { if (it.testName.startsWith("Another") && it.bluetoothEnergyConsumption?.common != null) { File("/home/rinatisk/jsons/usb/another_usb_b.txt").appendText(
            it.bluetoothEnergyConsumption.common.toString() + " "
        ) } }
               analyzeResult.forEach { if (it.testName.startsWith("Another") && it.wifiEnergyConsumption?.common != null) { File("/home/rinatisk/jsons/usb/another_usb_w.txt").appendText(
            it.wifiEnergyConsumption.common.toString() + " "
        ) } }

        analyzeResult.forEach { if (it.testName.startsWith("Navi") && it.bluetoothEnergyConsumption?.common != null) { File("/home/rinatisk/jsons/usb/navigation_usb_b.txt").appendText(
            it.bluetoothEnergyConsumption.common.toString() + " "
        ) } }
        analyzeResult.forEach { if (it.testName.startsWith("Navi") && it.wifiEnergyConsumption?.common != null) { File("/home/rinatisk/jsons/usb/navigation_usb_w.txt").appendText(
            it.wifiEnergyConsumption.common.toString() + " "
        ) } }

        analyzeResult.forEach { if (it.testName.startsWith("Blu") && it.bluetoothEnergyConsumption?.common != null) { File("/home/rinatisk/jsons/wifi/bluetooth_wifi_b.txt").appendText(
            it.bluetoothEnergyConsumption.common.toString() + " "
        ) } }
        analyzeResult.forEach { if (it.testName.startsWith("Blu") && it.wifiEnergyConsumption?.common != null) { File("/home/rinatisk/jsons/wifi/bluetooth_wifi_w.txt").appendText(
            it.wifiEnergyConsumption.common.toString() + " "
        ) } }

        analyzeResult.forEach { if (it.testName.startsWith("Wi") && it.bluetoothEnergyConsumption?.common != null) { File("/home/rinatisk/jsons/wifi/wifi_wifi_b.txt").appendText(
            it.bluetoothEnergyConsumption.common.toString() + " "
        ) } }
        analyzeResult.forEach { if (it.testName.startsWith("Wi") && it.wifiEnergyConsumption?.common != null) { File("/home/rinatisk/jsons/wifi/wifi_wifi_w.txt").appendText(
            it.wifiEnergyConsumption.common.toString() + " "
        ) } }


        //  File("/home/rinatisk/jsons/sum.txt").writeText()

        println(analyzeResult.map { "${it.testName} ${it.wifiEnergyConsumption?.common} ${it.bluetoothEnergyConsumption?.common} ${it.cpuEnergyConsumption.cpu}\n ${it.energy} ${it.memory} ${it.packets}"}.toString())
     //   sum.appendText(analyzeResult.map { ("${it.testName} ${it.cpuEnergyConsumption.cpu} ${it.wifiEnergyConsumption?.wifi ?: 0} ${it.bluetoothEnergyConsumption?.bluetooth ?: 0} " )}.toString() + "\n")
    }
}


object ProfilerResultParser {
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
            val connectionInfo = mutableListOf<ConnectionInfo>()
            val uidInfo = mutableListOf<UidInfo>()

            for(component in testLogs.keys()) {
                if(component != null) {
                    when(component) {
                        "Wi-Fi" -> {
                            val array = testLogs.getJSONObject("Wi-Fi")
                                connectionInfo.add(parseConnectionInfo(array))
                        }
                        "Uid" -> {
                            val array = testLogs.getJSONObject("Uid")
                            uidInfo.add(parseUidInfo(array))
                        }
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
                if(bluetoothInfo.size != 0) bluetoothInfo else null,
                uidInfo,
                connectionInfo
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

    private fun parseConnectionInfo(obj: JSONObject): ConnectionInfo {

        var memory = Float.NaN
        var packets = 0

        val testDetails = obj.getJSONObject("body")
        for(component in testDetails.keys()) {
            if(component != null)
            {
                when (component) {
                    "packets" -> {
                        packets = testDetails.getInt(component)
                    }
                    "memory" -> {
                        memory = testDetails.getFloat(component)
                    }
                    else -> {
                        throw NullPointerException("Invalid json log")
                    }
                }
            }
        }

        return ConnectionInfo(memory, packets)
    }

    private fun parseUidInfo(obj: JSONObject): UidInfo {

        var energy = Float.NaN

        energy = obj.getFloat("body")

        return UidInfo(energy)
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