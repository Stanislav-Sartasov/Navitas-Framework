package data.model

class RawProfilingResult {

    private val map = mutableMapOf<String, List<RawMethodLog>>()

    fun addTestResults(testName: String, results: List<RawMethodLog>) {
        map[testName] = results
    }

    fun getTestResults(): List<Pair<String, List<RawMethodLog>>> {
        val result = mutableListOf<Pair<String, List<RawMethodLog>>>()
        for (entry in map.entries) {
            result.add(entry.key to entry.value)
        }
        return result
    }
}