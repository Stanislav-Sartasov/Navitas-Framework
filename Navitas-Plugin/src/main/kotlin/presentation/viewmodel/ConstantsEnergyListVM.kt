package presentation.viewmodel

import domain.model.EnergyConstant
import domain.repository.ConstantsResultRepository
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ConstantsEnergyListVM (
    private val constantsResultRepository: ConstantsResultRepository
) {

    private val energyConstantSubject = PublishSubject.create<List<EnergyConstant>>()
    val energyConstant: Observable<List<EnergyConstant>> = energyConstantSubject

    fun fetch() {
        constantsResultRepository.fetchEnergyConstants()
            .subscribe( { result ->
                energyConstantSubject.onNext(result)
            }, { error ->
                error.printStackTrace()
            })
    }
}