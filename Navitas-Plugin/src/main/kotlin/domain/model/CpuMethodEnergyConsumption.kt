package domain.model

class CpuMethodEnergyConsumption (
        val methodName: String,
        val startTimestamp: Long,
        val endTimestamp: Long,
        val cpuEnergy: Float,
        val nestedMethods: List<CpuMethodEnergyConsumption>
)
