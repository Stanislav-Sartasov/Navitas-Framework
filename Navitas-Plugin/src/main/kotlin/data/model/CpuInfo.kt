package data.model

import data.model.components.*

data class CpuInfo (
    val methodInfo: MethodInfo,
    val cpuTimeInStates: CpuTimeInStates,
    val brightnessLevel: Int
)