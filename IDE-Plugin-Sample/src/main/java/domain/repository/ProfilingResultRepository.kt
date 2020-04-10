package domain.repository

import domain.model.TestEnergyConsumption

interface ProfilingResultRepository {

    fun fetchEnergyTrace()
    fun fetchEnergyConsumption(): List<TestEnergyConsumption>
}