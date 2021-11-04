package com.vhontar.anynotes.business.data.util

import com.vhontar.anynotes.business.data.cache.CacheConstants
import com.vhontar.anynotes.business.data.cache.CacheErrors
import com.vhontar.anynotes.business.data.cache.CacheResult
import com.vhontar.anynotes.business.data.network.NetworkConstants
import com.vhontar.anynotes.business.data.network.NetworkErrors
import com.vhontar.anynotes.business.data.network.NetworkResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException

/**
 * Reference: https://medium.com/@douglas.iacovelli/how-to-handle-errors-with-retrofit-and-coroutines-33e7492a912
 */
suspend fun <T> safeNetworkCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): NetworkResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(NetworkConstants.NETWORK_TIMEOUT) {
                NetworkResult.Success(apiCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is TimeoutCancellationException -> {
                    val code = 408 // timeout error code
                    NetworkResult.GenericError(code, NetworkErrors.NETWORK_ERROR_TIMEOUT)
                }
                is IOException -> {
                    NetworkResult.NetworkError
                }
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    NetworkResult.GenericError(
                        code,
                        errorResponse
                    )
                }
                else -> {
                    NetworkResult.GenericError(
                        null,
                        NetworkErrors.NETWORK_ERROR_UNKNOWN
                    )
                }
            }
        }
    }
}

suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher,
    cacheCall: suspend () -> T?
): CacheResult<T?> {
    return withContext(dispatcher) {
        try {
            // throws TimeoutCancellationException
            withTimeout(CacheConstants.CACHE_TIMEOUT) {
                CacheResult.Success(cacheCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is TimeoutCancellationException -> {
                    CacheResult.GenericError(CacheErrors.CACHE_TIMEOUT_ERROR)
                }
                else -> {
                    CacheResult.GenericError(CacheErrors.CACHE_ERROR_UNKNOWN)
                }
            }
        }
    }
}

private fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.response()?.errorBody()?.string()
    } catch (exception: Exception) {
        GenericErrors.ERROR_UNKNOWN
    }
}
