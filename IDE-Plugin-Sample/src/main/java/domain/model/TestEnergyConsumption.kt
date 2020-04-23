package domain.model

@Deprecated("This class is used only for generating dummy data and will be removed in the future")
data class TestEnergyConsumption(
        val testName: String,
        val methodDetails: List<EnergyConsumption>
)