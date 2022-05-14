package data.model

import data.model.components.GpuEnergyConsumption

data class GpuInfo(
    val testInfo: TestInfo,
    val gpuEnergyConsumption: GpuEnergyConsumption
)