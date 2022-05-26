package domain.model

data class EnergyConsumption (
        val consumer: String,
        val cpuEnergy: Float,
        val wifiEnergy: Float,
        val bluetoothEnergy: Float,
        val gpuEnergy: Float,
        val displayEnergy: Float
)