package domain.repository

import domain.model.PowerProfile
import io.reactivex.Observable

interface PowerProfileRepository {

    fun fetch(): Observable<PowerProfile>
    fun save(powerProfile: PowerProfile)
}