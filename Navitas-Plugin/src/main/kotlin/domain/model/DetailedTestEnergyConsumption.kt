package domain.model

import data.model.components.BluetoothEnergyConsumption
import data.model.components.WifiEnergyConsumption

class DetailedTestEnergyConsumption(
    val testName: String,
    val cpuEnergyConsumption: CpuEnergyConsumption,
    val wifiEnergyConsumption: WifiEnergyConsumption?,
    val bluetoothEnergyConsumption: BluetoothEnergyConsumption?,
    val memory: Float = Float.NaN,
    val packets: Int = 0,
    val energy: Float = Float.NaN
)