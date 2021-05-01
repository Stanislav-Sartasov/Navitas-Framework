package domain.model

class CpuEnergyConsumption(
    val energy: Float,
    val testDetails: Map<Pair<Int, Int>, List<CpuMethodEnergyConsumption>>
)

