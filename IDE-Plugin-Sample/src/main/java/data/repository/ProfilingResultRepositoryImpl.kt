package data.repository

import domain.model.DetailedTestEnergyConsumption
import domain.model.EnergyConsumption
import domain.repository.ProfilingResultRepository
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

object ProfilingResultRepositoryImpl : ProfilingResultRepository {

    private val energyConsumptionSubject = BehaviorSubject.create<List<DetailedTestEnergyConsumption>>()

    private var currentProfilingResult: List<DetailedTestEnergyConsumption>? = null

    // TODO: Add executor
    override fun fetchTestsEnergyConsumption(): Single<List<EnergyConsumption>> {
        return Single.fromCallable {
            currentProfilingResult?.map { details ->
                EnergyConsumption(details.testName, details.energy)
            }
        }
    }

    // TODO: Add executor
    override fun fetchDetailedTestEnergyConsumption(position: Int): Single<DetailedTestEnergyConsumption> {
        return Single.fromCallable {
            currentProfilingResult?.get(position)
        }
    }

    override fun save(result: List<DetailedTestEnergyConsumption>) {
        currentProfilingResult = result
        energyConsumptionSubject.onNext(result)
    }
}