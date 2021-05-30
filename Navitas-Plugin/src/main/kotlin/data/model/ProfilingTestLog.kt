package data.model

data class ProfilingTestLog (
        val cpuInfo : List<CpuInfo>,
        val wifiInfo : List<WifiInfo>?,
        val bluetoothInfo : List<BluetoothInfo>?
)