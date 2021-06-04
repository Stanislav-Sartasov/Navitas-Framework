package tooling

import data.model.BluetoothInfo
import data.model.ProfilingResult
import data.model.WifiInfo
import domain.model.EnergyConstant
import org.jetbrains.kotlin.idea.kdoc.Placeholder
import org.jetbrains.kotlin.idea.kdoc.insert

object ConstantsResultAnalyzer {

    fun analyze(profilingResult: ProfilingResult): List<EnergyConstant> {
        val constantsList = mutableListOf<EnergyConstant>()
        for (testResult in profilingResult.getTestResults()) {
            val testNameParts = testResult.first.split('.')
            val freqAndConstantMap = mutableMapOf<Double, Double>()
            when (testNameParts[0]) {
                "WifiConstants" -> {
                    val wifiMap = testResult.second.wifiInfo?.let { wifiTestLogConverter(it) }
                    wifiMap?.forEach { it ->
                        freqAndConstantMap[it.key.toDouble()] = it.value.average()
                    }
                    if (freqAndConstantMap.isNotEmpty()) {
                        val wifiRegressionModel = LinearRegression(freqAndConstantMap.keys.toList(), freqAndConstantMap.values.toList())
                        val wifiConstant = wifiRegressionModel.predict(0.0).toFloat()
                        constantsList.add(EnergyConstant(normalizeComponent(testNameParts[1]), wifiConstant))
                    }
                }
                "BluetoothConstants" -> {
                    val bluetoothMap = testResult.second.bluetoothInfo?.let { bluetoothTestLogConverter(it) }
                    bluetoothMap?.forEach { it ->
                        freqAndConstantMap[it.key.toDouble()] = it.value.average()
                    }
                    if (freqAndConstantMap.isNotEmpty()) {
                        val bluetoothRegressionModel = LinearRegression(freqAndConstantMap.keys.toList(), freqAndConstantMap.values.toList())
                        val bluetoothConstant = bluetoothRegressionModel.predict(0.0).toFloat()
                        constantsList.add(EnergyConstant(normalizeComponent(testNameParts[1]), bluetoothConstant))
                    }
                }
            }
        }
        return constantsList
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

    private fun normalizeComponent(name : String) : String {
        val upper = name.find { char -> char.toUpperCase() == char }!!
        name.replaceFirst(upper.toString(), ".$upper", false)

        return name.toLowerCase()
    }
}