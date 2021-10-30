package com.vhontar.notes.business.data

import com.vhontar.notes.business.domain.state.DataState

interface ResponseHandler<ViewState, Data> {
    fun getResult(): DataState<ViewState>
    fun handleResponse(responseObj: Data): DataState<ViewState>
}