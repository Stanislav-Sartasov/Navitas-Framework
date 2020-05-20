package data.repository

import domain.model.ProfilingConfiguration
import domain.repository.ConfigurationRepository
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class ConfigurationRepositoryImpl : ConfigurationRepository {

    private val profilingConfigurationSubject = BehaviorSubject.create<ProfilingConfiguration>()

    var isEmpty = true
        private set

    override fun fetch(): Observable<ProfilingConfiguration> = profilingConfigurationSubject

    override fun save(config: ProfilingConfiguration) {
        isEmpty = false
        profilingConfigurationSubject.onNext(config)
    }
}