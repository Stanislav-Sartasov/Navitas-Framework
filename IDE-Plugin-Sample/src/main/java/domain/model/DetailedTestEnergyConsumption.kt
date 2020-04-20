package domain.model

import data.model.MethodDetails

class DetailedTestEnergyConsumption(
           val testName: String,
           val testDetails: Map<Pair<Int, Int>, List<MethodDetails>>
) : ProfilingResult