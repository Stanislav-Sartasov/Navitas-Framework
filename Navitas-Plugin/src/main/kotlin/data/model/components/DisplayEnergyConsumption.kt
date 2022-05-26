package data.model.components

data class DisplayEnergyConsumption(
    val common: Float,
    val display: Float,
    val external: List<ComponentEnergyPair>?
)