package data.model.components

data class WifiEnergyConsumption (
    val common : Float,
    val wifi : Float,
    val external : List<ComponentEnergyPair>?
)