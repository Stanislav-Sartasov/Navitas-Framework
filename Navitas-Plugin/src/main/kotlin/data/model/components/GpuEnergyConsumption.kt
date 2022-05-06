package data.model.components

data class GpuEnergyConsumption(
    val common: Float,
    val gpu: Float,
    val external: List<ComponentEnergyPair>?
)