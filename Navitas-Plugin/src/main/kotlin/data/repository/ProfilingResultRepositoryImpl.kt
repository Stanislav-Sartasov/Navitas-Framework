package data.repository

import domain.model.DetailedTestEnergyConsumption
import domain.model.EnergyConsumption
import domain.repository.ProfilingResultRepository
import io.reactivex.Single

class ProfilingResultRepositoryImpl : ProfilingResultRepository {

    private var currentProfilingResult: List<DetailedTestEnergyConsumption>? = null

    override fun fetchTestsEnergyConsumption(): Single<List<EnergyConsumption>> {
        return Single.fromCallable {
            currentProfilingResult?.map { details ->
                EnergyConsumption(
                    details.testName,
                    details.cpuEnergyConsumption.cpu,
                    details.wifiEnergyConsumption?.wifi ?: Float.NaN,
                    details.bluetoothEnergyConsumption?.bluetooth ?: Float.NaN,
                    details.gpuEnergyConsumption?.gpu ?: Float.NaN
                )
            }
        }
    }

    override fun fetchDetailedTestEnergyConsumption(position: Int): Single<DetailedTestEnergyConsumption> {
        return Single.fromCallable {
            currentProfilingResult?.get(position)
        }
    }

    override fun save(result: List<DetailedTestEnergyConsumption>) {
        currentProfilingResult = result
    }
}