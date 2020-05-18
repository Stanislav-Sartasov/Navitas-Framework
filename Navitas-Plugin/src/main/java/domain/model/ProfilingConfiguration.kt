package domain.model

class ProfilingConfiguration(
        val moduleName: String,
        val modulePath: String,
        val instrumentedTestNames: Map<String, List<String>>
)