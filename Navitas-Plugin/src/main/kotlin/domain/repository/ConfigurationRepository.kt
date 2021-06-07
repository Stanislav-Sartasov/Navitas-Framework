package domain.repository

import domain.model.ProfilingConfiguration
import io.reactivex.Observable

interface ConfigurationRepository {

    fun fetch(): Observable<ProfilingConfiguration>
    fun save(config: ProfilingConfiguration)
    fun switchMode(mode: String)
}