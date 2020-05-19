package data.repository

import domain.model.PowerProfile
import domain.repository.PowerProfileRepository
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class PowerProfileRepositoryImpl : PowerProfileRepository {

    private val powerProfileSubject = BehaviorSubject.create<PowerProfile>()

    var isEmpty = true
        private set

    override fun fetch(): Observable<PowerProfile> = powerProfileSubject

    override fun save(powerProfile: PowerProfile) {
        isEmpty = false
        powerProfileSubject.onNext(powerProfile)
    }
}