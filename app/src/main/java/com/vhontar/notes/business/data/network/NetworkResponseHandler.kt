package com.vhontar.notes.business.data.network

import com.vhontar.notes.business.data.ResponseHandler
import com.vhontar.notes.business.domain.state.*

abstract class NetworkResponseHandler<ViewState, Data>(
    private val response: NetworkResult<Data?>,
    private val stateEvent: StateEvent?
) : ResponseHandler<ViewState, Data> {
    override fun getResult(): DataState<ViewState> {
        return when (response) {
            is NetworkResult.GenericError -> {
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
            is NetworkResult.NetworkError -> {
                DataState.error(
                    response = Response(
                        message = "${stateEvent?.eventName()}\n\n" +
                                "Reason: ${NetworkErrors.NETWORK_ERROR}",
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    ),
                    stateEvent
                )
            }
            is NetworkResult.Success -> {
                if (response.data == null) {
                    DataState.error(
                        response = Response(
                            message = "${stateEvent?.eventName()}\n\n" +
                                    "Reason: ${NetworkErrors.NETWORK_DATA_NULL}",
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Error
                        ),
                        stateEvent
                    )
                } else {
                    handleResponse(response.data)
                }
            }
        }
    }
}