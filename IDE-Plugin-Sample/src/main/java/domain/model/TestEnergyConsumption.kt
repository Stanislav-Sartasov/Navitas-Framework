package domain.model

data class TestEnergyConsumption(
        val testName: String,
        val methodDetails: List<EnergyConsumption>
) : ProfilingResult