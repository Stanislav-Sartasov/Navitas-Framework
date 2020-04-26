package domain.model

data class TestInfo(
        val testName: String,
        val energy: Float,
        val processThreadIDs: List<Pair<Int, Int>>
)