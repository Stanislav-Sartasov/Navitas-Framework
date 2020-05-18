package data.repository

import data.model.dummyProfilingConfiguration
import domain.model.ProfilingConfiguration
import domain.repository.ConfigurationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class ConfigurationRepositoryTests {

    private val repository: ConfigurationRepository = ConfigurationRepositoryImpl()

    @Test
    fun saveDataAndCheckUpdate() {
        var fetchedConfig: ProfilingConfiguration? = null

        repository.save(dummyProfilingConfiguration)
        repository.fetch()
                .subscribe { config ->
                    fetchedConfig = config
                }

        fetchedConfig?.let {
            assertEquals(it, dummyProfilingConfiguration, "Profiling configuration was updated incorrectly")
        } ?: fail("Current profiling configuration wasn't updated")
    }
}