package data.model

import data.model.components.WifiEnergyConsumption

data class WifiInfo (
    val testInfo : TestInfo,
    val wifiEnergyConsumption: WifiEnergyConsumption
)