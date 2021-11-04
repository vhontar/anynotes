package com.vhontar.anynotes.business.data

import com.vhontar.anynotes.business.domain.state.DataState

interface ResponseHandler<ViewState, Data> {
    fun getResult(): DataState<ViewState>?
    fun handleResponse(responseObj: Data): DataState<ViewState>?
}