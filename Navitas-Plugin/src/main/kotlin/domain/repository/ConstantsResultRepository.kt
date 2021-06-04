package domain.repository

import domain.model.EnergyConstant
import io.reactivex.Single

interface ConstantsResultRepository {
    fun fetchEnergyConstants(): Single<List<EnergyConstant>>
    fun save(result: List<EnergyConstant>)
}