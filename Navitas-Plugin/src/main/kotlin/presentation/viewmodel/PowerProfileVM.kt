package presentation.viewmodel

import domain.model.PowerProfile
import domain.repository.PowerProfileRepository
import io.reactivex.Observable
import tooling.PowerProfileManager
import tooling.PowerProfileParser
import java.io.File

class PowerProfileVM(private val powerProfileRepository: PowerProfileRepository) {

    val powerProfile: Observable<PowerProfile> = powerProfileRepository.fetch()

    fun saveDefault() {
        val profile = PowerProfileManager.getDefaultProfile()
        powerProfileRepository.save(profile)
    }

    fun save(profileFile: File) {
        val profile = PowerProfileParser(profileFile).parseXML()
        powerProfileRepository.save(profile)
    }
}