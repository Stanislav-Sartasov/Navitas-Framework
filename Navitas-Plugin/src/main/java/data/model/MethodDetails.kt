package data.model

class MethodDetails(
        val methodName: String,
        val startTimestamp: Long,
        val endTimestamp: Long,
        val cpuEnergy: Float,
        val nestedMethods: List<MethodDetails>
)

