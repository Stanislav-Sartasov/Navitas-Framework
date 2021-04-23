package data.model

import data.model.components.*

data class ProfilingTestLog (
        val cpuInfo : List<CpuInfo>,
        val wifiEnergyConsumption : WifiEnergyConsumption?,
        val bluetoothEnergyConsumption : BluetoothEnergyConsumption?
)