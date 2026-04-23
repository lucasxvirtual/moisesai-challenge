package com.example.moisesaichallenge.core.network

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val throwable: Throwable) : NetworkResult<Nothing>()
}

fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.Error -> this
}

suspend fun <T> safeApiCall(call: suspend () -> T): NetworkResult<T> = try {
    NetworkResult.Success(call())
} catch (e: Exception) {
    NetworkResult.Error(e)
}
