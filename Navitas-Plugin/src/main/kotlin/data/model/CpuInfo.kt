package data.model

import data.model.components.CpuTimeInStates

data class CpuInfo (
    val methodInfo: MethodInfo,
    val cpuTimeInStates: CpuTimeInStates,
    val brightnessLevel: Int
)