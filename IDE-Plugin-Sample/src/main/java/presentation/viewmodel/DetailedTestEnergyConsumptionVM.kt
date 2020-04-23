package presentation.viewmodel

import domain.model.DetailedTestEnergyConsumption
import domain.repository.ProfilingResultRepository
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class DetailedTestEnergyConsumptionVM(
        private val profilingResultRepository: ProfilingResultRepository
) {

    private val energyConsumptionSubject = PublishSubject.create<DetailedTestEnergyConsumption>()
    val energyConsumption: Observable<DetailedTestEnergyConsumption> = energyConsumptionSubject

    fun fetch(position: Int) {
        profilingResultRepository.fetchDetailedTestEnergyConsumption(position)
                .subscribe( { result ->
                    energyConsumptionSubject.onNext(result)
                }, { error ->
                    // TODO: send error
                })
    }
}
