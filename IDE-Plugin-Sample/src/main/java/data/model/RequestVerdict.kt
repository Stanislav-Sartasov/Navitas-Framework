package data.model

sealed class RequestVerdict<T, E> {

    class Success<T, E>(val result: T) : RequestVerdict<T, E>()

    class Failure<T, E>(val error: E) : RequestVerdict<T, E>()
}