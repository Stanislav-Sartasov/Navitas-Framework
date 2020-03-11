package ui.view_models

import data.ConfigurationRepository
import data.model.ProfilingConfiguration

class ConfiguringViewModel {

    fun save(config: ProfilingConfiguration) {
        ConfigurationRepository.saveProfilingConfiguration(config)
    }
}