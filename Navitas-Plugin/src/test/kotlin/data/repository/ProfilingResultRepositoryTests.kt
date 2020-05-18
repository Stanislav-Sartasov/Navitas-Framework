package data.repository

import data.model.dummyProfilingResult
import data.model.dummyTestEnergyConsumptionList
import domain.model.EnergyConsumption
import domain.repository.ProfilingResultRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class ProfilingResultRepositoryTests {

    private val repository: ProfilingResultRepository = ProfilingResultRepositoryImpl()

    @Test
    fun saveDataAndCheckUpdate() {
        var fetchedResult: List<EnergyConsumption>? = null

        repository.save(dummyProfilingResult)
        repository.fetchTestsEnergyConsumption()
                .subscribe { result ->
                    fetchedResult = result
                }

        fetchedResult?.let {
            assertEquals(it, dummyTestEnergyConsumptionList, "Test energy consumption list was calculated incorrectly")
        } ?: fail("Current profiling result wasn't updated")
    }
}