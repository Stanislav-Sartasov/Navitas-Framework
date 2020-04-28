package data.model

sealed class ProfilingError {

    class EmptyConfigurationError : ProfilingError()

    class FailedTaskExecutionError : ProfilingError()
}