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

    companion object {
        val ALL_PROCESSES_AND_THREADS = -1 to -1
    }

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

                    val ids = result.testDetails.keys.toMutableList()
                    ids.add(0, ALL_PROCESSES_AND_THREADS)

                    currentProcessThreadIDs = ids[0]
                    testInfoSubject.onNext(TestInfo(result.testName, result.energy, ids))
                    energyConsumptionSubject.onNext(cache!!.testDetails.values.flatten())
//                    energyConsumptionSubject.onNext(result.testDetails.getOrDefault(currentProcessThreadIDs!!, emptyList()))
                }, { error ->
                    // TODO: send error
                })
    }

    fun fetch(processThreadIDs: Pair<Int, Int>) {
        if (processThreadIDs != currentProcessThreadIDs) {
            currentProcessThreadIDs = processThreadIDs
            if (processThreadIDs == ALL_PROCESSES_AND_THREADS) {
                energyConsumptionSubject.onNext(cache!!.testDetails.values.flatten())
            } else {
                energyConsumptionSubject.onNext(cache!!.testDetails.getOrDefault(processThreadIDs, emptyList()))
            }
        }
    }
}
