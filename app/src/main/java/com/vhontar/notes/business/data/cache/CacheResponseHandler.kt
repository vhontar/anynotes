package com.vhontar.notes.business.data.cache

import com.vhontar.notes.business.data.ResponseHandler
import com.vhontar.notes.business.domain.state.*

abstract class CacheResponseHandler<ViewState, Data>(
    private val response: CacheResult<Data?>,
    private val stateEvent: StateEvent?
) : ResponseHandler<ViewState, Data> {
    override fun getResult(): DataState<ViewState> {
        return when (response) {
            is CacheResult.GenericError -> {
                DataState.error(
                    response = Response(
                        message = "${stateEvent?.eventName()}\n\n" +
                                "Reason: ${response.errorMessage}",
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    ),
                    stateEvent
                )
            }
            is CacheResult.Success -> {
                if (response.data == null) {
                    DataState.error(
                        response = Response(
                            message = "${stateEvent?.eventName()}\n\n" +
                                    "Reason: ${CacheErrors.CACHE_DATA_NULL}",
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Error
                        ),
                        stateEvent
                    )
                } else {
                    handleResponse(responseObj = response.data)
                }
            }
        }
    }
}