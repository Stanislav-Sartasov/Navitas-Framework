package domain.model

class DetailedTestEnergyConsumption(
           val testName: String,
           val energy: Float,
           val testDetails: Map<Pair<Int, Int>, List<MethodEnergyConsumption>>
)