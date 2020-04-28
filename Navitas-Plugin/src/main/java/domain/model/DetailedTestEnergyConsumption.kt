package domain.model

import data.model.MethodDetails

class DetailedTestEnergyConsumption(
           val testName: String,
           val energy: Float,
           val testDetails: Map<Pair<Int, Int>, List<MethodDetails>>
)