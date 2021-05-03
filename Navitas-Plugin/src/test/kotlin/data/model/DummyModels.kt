package data.model

import data.model.components.BluetoothEnergyConsumption
import data.model.components.WifiEnergyConsumption
import domain.model.CpuEnergyConsumption
import domain.model.DetailedTestEnergyConsumption
import domain.model.EnergyConsumption
import domain.model.ProfilingConfiguration

val dummyProfilingConfiguration = ProfilingConfiguration("module name", "module path", mapOf())

val dummyProfilingResult = listOf(
        DetailedTestEnergyConsumption("test 1", CpuEnergyConsumption(0.0F, mapOf()),
                WifiEnergyConsumption(0.0F, 0.0F, listOf()), BluetoothEnergyConsumption(0.0F, 0.0F, listOf()))
)

val dummyTestEnergyConsumptionList = listOf(
        EnergyConsumption("test 1", 0.0F, 0f, 0f)
)