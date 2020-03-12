package data

import data.model.ProfilingConfiguration
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

object ConfigurationRepository {

    private val profilingConfigurationSubject = BehaviorSubject.create<ProfilingConfiguration>()
    val profilingConfiguration: Observable<ProfilingConfiguration> = profilingConfigurationSubject

    var isEmpty = true
        private set

    fun saveProfilingConfiguration(config: ProfilingConfiguration) {
        isEmpty = false
        profilingConfigurationSubject.onNext(config)
    }
}