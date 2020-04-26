package presentation.viewmodel

import data.model.MethodDetails
import domain.model.DetailedTestEnergyConsumption
import domain.model.TestInfo
import domain.repository.ProfilingResultRepository
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class DetailedTestEnergyConsumptionVM(
        private val profilingResultRepository: ProfilingResultRepository
) {

    private val testInfoSubject = PublishSubject.create<TestInfo>()
    val testInfo: Observable<TestInfo> = testInfoSubject

    private val energyConsumptionSubject = PublishSubject.create<List<MethodDetails>>()
    val energyConsumption: Observable<List<MethodDetails>> = energyConsumptionSubject

    private var cache: DetailedTestEnergyConsumption? = null
    private var currentProcessThreadIDs: Pair<Int, Int>? = null

    fun fetch(position: Int) {
        profilingResultRepository.fetchDetailedTestEnergyConsumption(position)
                .subscribe( { result ->
                    cache = result
                    testInfoSubject.onNext(TestInfo(result.testName, result.energy, result.testDetails.keys.toList()))
                }, { error ->
                    // TODO: send error
                })
    }

    fun fetch(processThreadIDs: Pair<Int, Int>) {
        if (processThreadIDs != currentProcessThreadIDs) {
            currentProcessThreadIDs = processThreadIDs
            energyConsumptionSubject.onNext(cache!!.testDetails[processThreadIDs]!!)
        }
    }
}
