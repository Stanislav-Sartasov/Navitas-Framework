package data.repository

import domain.model.PowerProfile
import domain.repository.PowerProfileRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import tooling.PowerProfileManager

class PowerProfileRepositoryTest {

    private val repository: PowerProfileRepository = PowerProfileRepositoryImpl()

    @Test
    fun saveDataAndCheckUpdate() {
        var fetchedProfile: PowerProfile? = null
        val savedProfile = PowerProfileManager.getDefaultProfile()

        repository.save(savedProfile)
        repository.fetch()
                .subscribe { profile ->
                    fetchedProfile = profile
                }

        fetchedProfile?.let {
            Assertions.assertEquals(it, savedProfile, "Power profile was updated incorrectly")
        } ?: Assertions.fail("Current power profile wasn't updated")
    }
}