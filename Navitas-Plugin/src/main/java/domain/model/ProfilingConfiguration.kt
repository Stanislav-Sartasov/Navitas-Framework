package domain.model

import com.intellij.openapi.module.Module

class ProfilingConfiguration(
        val module: Module,
        val instrumentedTestNames: Map<String, List<String>>
)