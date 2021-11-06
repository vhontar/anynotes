package com.vhontar.anynotes.business.data

import com.vhontar.anynotes.business.domain.state.DataState

interface ResponseHandler<T, V> {
    fun getResult(): DataState<T>?
    fun handleResponse(responseObj: V): DataState<T>?
}