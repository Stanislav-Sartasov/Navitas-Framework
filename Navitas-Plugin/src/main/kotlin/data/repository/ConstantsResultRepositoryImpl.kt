package data.repository

import domain.model.EnergyConstant
import domain.repository.ConstantsResultRepository
import io.reactivex.Single

class ConstantsResultRepositoryImpl : ConstantsResultRepository {

    private var currentConstantsResult: List<EnergyConstant>? = null

    override fun fetchEnergyConstants(): Single<List<EnergyConstant>> {
        return Single.fromCallable {
            currentConstantsResult?.map { result ->
                EnergyConstant(result.component, result.constant)
            }
        }
    }

    override fun save(result: List<EnergyConstant>) {
        currentConstantsResult = result
    }
}