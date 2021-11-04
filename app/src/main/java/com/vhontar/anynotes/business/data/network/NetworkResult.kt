package com.vhontar.anynotes.business.data.network

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class GenericError(
        val code: Int? = null,
        val errorMessage: String? = null
    ) : NetworkResult<Nothing>()

    object NetworkError : NetworkResult<Nothing>()
}