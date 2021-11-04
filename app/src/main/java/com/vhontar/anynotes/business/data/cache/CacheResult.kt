package com.vhontar.anynotes.business.data.cache

sealed class CacheResult<out T> {
    data class Success<out T>(val data: T): CacheResult<T>()
    data class GenericError(val errorMessage: String? = null): CacheResult<Nothing>()
}
