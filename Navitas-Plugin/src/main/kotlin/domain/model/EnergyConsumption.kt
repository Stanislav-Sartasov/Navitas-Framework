package domain.model

data class EnergyConsumption (
        val consumer: String,
        val cpuEnergy: Float,
        val wifiEnergy: Float,
        val bluetoothEnergy: Float,
        val memory: Float = Float.NaN,
        val packets: Int = 0,
        val energy: Float = Float.NaN
)