package data.model.components

data class BluetoothEnergyConsumption (
    val common : Float,
    val bluetooth : Float,
    val external : List<ComponentEnergyPair>?
)