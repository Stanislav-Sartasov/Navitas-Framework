package data.model.components

data class CpuTimeInStates (
        val coreTimeInStates: List<List<CoreTimeAtFrequency>>
)

data class CoreTimeAtFrequency (
        val frequency: Long,
        val timestamp: Long
)