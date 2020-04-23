package presentation.viewmodel

import domain.model.ProfilingConfiguration
import domain.repository.ConfigurationRepository
import io.reactivex.Observable

class ConfiguringVM(
        private val configurationRepository: ConfigurationRepository
) {

    val profilingConfiguration: Observable<ProfilingConfiguration> = configurationRepository.fetch()

    fun save(config: ProfilingConfiguration) {
        configurationRepository.save(config)
    }
}