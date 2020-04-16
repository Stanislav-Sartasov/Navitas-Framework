package data.repository

import data.generateTestEnergyConsumptionList
import domain.model.TestEnergyConsumption
import domain.repository.ProfilingResultRepository

object ProfilingResultRepositoryImpl : ProfilingResultRepository {

    override fun fetchEnergyTrace() {}

    override fun fetchEnergyConsumption(): List<TestEnergyConsumption> {
        return generateTestEnergyConsumptionList(10)
    }
}