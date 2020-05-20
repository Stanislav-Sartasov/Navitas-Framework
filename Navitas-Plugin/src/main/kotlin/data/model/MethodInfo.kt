package data.model

data class MethodInfo(
        val methodName: String,
        val timestamp: Long,
        val processID: Int,
        val threadID: Int,
        val isEntry: Boolean
)