package ui.view_models

import data.ConfigurationRepository
import data.model.ProfilingConfiguration
import io.reactivex.Observable

class ConfiguringViewModel {

    val profilingConfiguration: Observable<ProfilingConfiguration> = ConfigurationRepository.profilingConfiguration

    fun save(config: ProfilingConfiguration) {
        ConfigurationRepository.saveProfilingConfiguration(config)
    }
}