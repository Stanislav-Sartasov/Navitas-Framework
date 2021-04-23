package domain.model

class MethodEnergyConsumption (
        val methodName: String,
        val startTimestamp: Long,
        val endTimestamp: Long,
        val cpuEnergy: Float,
        val nestedMethods: List<MethodEnergyConsumption>
)
