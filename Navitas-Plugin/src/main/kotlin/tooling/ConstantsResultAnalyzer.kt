package tooling

import data.model.BluetoothInfo
import data.model.ProfilingResult
import data.model.WifiInfo

object ConstantsResultAnalyzer {

    fun analyze(profilingResult: ProfilingResult): MutableMap<String, Float> {
        val constantsMap = mutableMapOf<String, Float>()
        for (testResult in profilingResult.getTestResults()) {
            val testName = testResult.first
            val freqAndConstantMap = mutableMapOf<Double, Double>()
            when (testName.substringBefore(".")) {
                "WifiConstant" -> {
                    val wifiMap = testResult.second.wifiInfo?.let { wifiTestLogConverter(it) }
                    wifiMap?.forEach { it ->
                        freqAndConstantMap[it.key.toDouble()] = it.value.average()
                    }
                    if (freqAndConstantMap.isNotEmpty()) {
                        val wifiRegressionModel = LinearRegression(freqAndConstantMap.keys.toList(), freqAndConstantMap.values.toList())
                        val wifiConstant = wifiRegressionModel.predict(0.0).toFloat()
                        constantsMap[testName.substringAfter("WifiConstant.")] = wifiConstant
                    }
                }
                "BluetoothConstant" -> {
                    val bluetoothMap = testResult.second.bluetoothInfo?.let { bluetoothTestLogConverter(it) }
                    bluetoothMap?.forEach { it ->
                        freqAndConstantMap[it.key.toDouble()] = it.value.average()
                    }
                    if (freqAndConstantMap.isNotEmpty()) {
                        val bluetoothRegressionModel = LinearRegression(freqAndConstantMap.keys.toList(), freqAndConstantMap.values.toList())
                        val bluetoothConstant = bluetoothRegressionModel.predict(0.0).toFloat()
                        constantsMap[testName.substringAfter("BluetoothConstant.")] = bluetoothConstant
                    }
                }
            }
        }
        return constantsMap
    }

    private fun wifiTestLogConverter(wifiInfoList: List<WifiInfo>): MutableMap<Float, List<Float>> {
        val wifiFrequenciesMap = mutableMapOf<Float, List<Float>>()
        (wifiInfoList.groupBy { it.testInfo.frequency } as Map<Float, MutableList<WifiInfo>>).forEach { it ->
            val wifiList = mutableListOf<Float>()
            it.value.sortBy { it.testInfo.timestamp }
            val startTime = it.value.first().testInfo.timestamp
            it.value.forEach { it ->
                wifiList.add(if (!it.wifiEnergyConsumption.wifi.isNaN()) it.wifiEnergyConsumption.wifi / ((it.testInfo.timestamp - startTime).toFloat() / (1000f * 3600f))
                else it.wifiEnergyConsumption.common / (it.testInfo.timestamp - startTime))
            }
            wifiList.removeAt(0)
            wifiFrequenciesMap[it.key] = wifiList
        }
        return wifiFrequenciesMap
    }

    private fun bluetoothTestLogConverter(bluetoothInfoList: List<BluetoothInfo>): MutableMap<Float, List<Float>> {
        val bluetoothFrequenciesMap = mutableMapOf<Float, List<Float>>()
        (bluetoothInfoList.groupBy { it.testInfo.frequency } as Map<Float, MutableList<BluetoothInfo>>).forEach { it ->
            val bluetoothList = mutableListOf<Float>()
            it.value.sortBy { it.testInfo.timestamp }
            val startTime = it.value.first().testInfo.timestamp
            it.value.forEach { it ->
                bluetoothList.add(if (!it.bluetoothEnergyConsumption.bluetooth.isNaN()) it.bluetoothEnergyConsumption.bluetooth / ((it.testInfo.timestamp - startTime).toFloat() / (1000f * 3600f))
                else it.bluetoothEnergyConsumption.common / (it.testInfo.timestamp - startTime))
            }
            bluetoothList.removeAt(0)
            bluetoothFrequenciesMap[it.key] = bluetoothList
        }
        return bluetoothFrequenciesMap
    }
}