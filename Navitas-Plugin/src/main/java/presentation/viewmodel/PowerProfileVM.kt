package presentation.viewmodel

import data.model.PowerProfile
import domain.repository.PowerProfileRepository
import io.reactivex.Observable

class PowerProfileVM(private val powerProfileRepository: PowerProfileRepository) {

    val powerProfile: Observable<PowerProfile> = powerProfileRepository.fetch()

    fun save(profile: PowerProfile) {
        powerProfileRepository.save(profile)
    }
}