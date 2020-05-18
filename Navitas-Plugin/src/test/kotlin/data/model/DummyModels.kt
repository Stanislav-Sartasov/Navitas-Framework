package data.model

import domain.model.DetailedTestEnergyConsumption
import domain.model.EnergyConsumption
import domain.model.ProfilingConfiguration

val dummyProfilingConfiguration = ProfilingConfiguration("module name", "module path", mapOf())

val dummyProfilingResult = listOf(
        DetailedTestEnergyConsumption("test 1", 0.0F, mapOf())
)

val dummyTestEnergyConsumptionList = listOf(
        EnergyConsumption("test 1", 0.0F)
)