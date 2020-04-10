package presentation.viewmodel

import domain.model.EnergyConsumption
import domain.model.FullEnergyConsumption
import domain.model.ProfilingResult
import domain.model.TestEnergyConsumption
import domain.repository.ProfilingResultRepository
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ProfilingResultViewModel(
        profilingResultRepository: ProfilingResultRepository
) {

    private val energyDistributionSubject = PublishSubject.create<ProfilingResult>()
    val energyDistribution: Observable<ProfilingResult> = energyDistributionSubject

    private val cache: List<TestEnergyConsumption> = profilingResultRepository.fetchEnergyConsumption()
    private val cachedFullEnergyConsumption =
            cache.map { data ->
                val testEnergy = data.methodDetails.fold(0F, { acc, elem -> elem.energy + acc })
                EnergyConsumption(data.testName, testEnergy)
            }

    fun fetch() {
        pushFullEnergyConsumption()
    }

    fun fetchTestDetails(testPosition: Int) {
        if (testPosition in cache.indices)
            pushTestEnergyConsumption(testPosition)
    }

    private fun pushFullEnergyConsumption() {
        energyDistributionSubject.onNext(FullEnergyConsumption(cachedFullEnergyConsumption))
    }

    private fun pushTestEnergyConsumption(testPosition: Int) {
        energyDistributionSubject.onNext(cache[testPosition])
    }
}