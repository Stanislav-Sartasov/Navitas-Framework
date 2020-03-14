package data.model

import com.intellij.openapi.module.Module

class ProfilingConfiguration(val module: Module, val instrumentedTestNames: List<String>)