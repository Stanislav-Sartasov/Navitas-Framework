package data.model

class ProfilingResult {

    private val map = mutableMapOf<String, ProfilingTestLog>()

    fun addTestResults(testName: String, results: ProfilingTestLog) {
        map[testName] = results
    }

    fun getTestResults(): List<Pair<String, ProfilingTestLog>> {
        val result = mutableListOf<Pair<String, ProfilingTestLog>>()
        for (entry in map.entries) {
            result.add(entry.key to entry.value)
        }

        return result
    }
}