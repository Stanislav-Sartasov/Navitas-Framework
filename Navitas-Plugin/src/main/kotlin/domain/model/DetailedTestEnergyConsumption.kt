package domain.model

import data.model.components.BluetoothEnergyConsumption
import data.model.components.DisplayEnergyConsumption
import data.model.components.GpuEnergyConsumption
import data.model.components.WifiEnergyConsumption

class DetailedTestEnergyConsumption(
    val testName: String,
    val cpuEnergyConsumption: CpuEnergyConsumption,
    val wifiEnergyConsumption: WifiEnergyConsumption?,
    val bluetoothEnergyConsumption: BluetoothEnergyConsumption?,
    val gpuEnergyConsumption: GpuEnergyConsumption?,
    val displayEnergyConsumption: DisplayEnergyConsumption?
)