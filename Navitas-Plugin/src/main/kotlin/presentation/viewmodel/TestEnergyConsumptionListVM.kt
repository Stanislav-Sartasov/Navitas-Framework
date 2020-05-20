package presentation.viewmodel

import domain.model.EnergyConsumption
import domain.repository.ProfilingResultRepository
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class TestEnergyConsumptionListVM(
        private val profilingResultRepository: ProfilingResultRepository
) {

    private val energyConsumptionSubject = PublishSubject.create<List<EnergyConsumption>>()
    val energyConsumption: Observable<List<EnergyConsumption>> = energyConsumptionSubject

    fun fetch() {
        profilingResultRepository.fetchTestsEnergyConsumption()
                .subscribe( { result ->
                    energyConsumptionSubject.onNext(result)
                }, { error ->
                    error.printStackTrace()
                })
    }
}