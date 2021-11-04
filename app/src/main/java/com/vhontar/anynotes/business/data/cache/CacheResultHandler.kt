package com.vhontar.anynotes.business.data.cache

import com.vhontar.anynotes.business.data.ResponseHandler
import com.vhontar.anynotes.business.domain.state.*

abstract class CacheResultHandler<ViewState, Data>(
    private val result: CacheResult<Data?>,
    private val stateEvent: StateEvent?
) : ResponseHandler<ViewState, Data> {
    override fun getResult(): DataState<ViewState>? {
        return when (result) {
            is CacheResult.GenericError -> {
                DataState.error(
                    response = Response(
                        message = "${stateEvent?.errorInfo()}\n\n" +
                                "Reason: ${result.errorMessage}",
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    ),
                    stateEvent
                )
            }
            is CacheResult.Success -> {
                if (result.data == null) {
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
                    handleResponse(responseObj = result.data)
                }
            }
        }
    }
}