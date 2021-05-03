package domain.model

class CpuEnergyConsumption(
    val cpu: Float,
    val testDetails: Map<Pair<Int, Int>, List<CpuMethodEnergyConsumption>>
)

