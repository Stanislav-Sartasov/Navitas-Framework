package domain.repository

import domain.model.DetailedTestEnergyConsumption
import domain.model.EnergyConsumption
import io.reactivex.Single

interface ProfilingResultRepository {

    fun fetchTestsEnergyConsumption(): Single<List<EnergyConsumption>>
    fun fetchDetailedTestEnergyConsumption(position: Int): Single<DetailedTestEnergyConsumption>
    fun save(result: List<DetailedTestEnergyConsumption>)
}