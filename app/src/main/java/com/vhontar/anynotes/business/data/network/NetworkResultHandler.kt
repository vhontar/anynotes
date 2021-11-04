package com.vhontar.anynotes.business.data.network

import com.vhontar.anynotes.business.data.ResponseHandler
import com.vhontar.anynotes.business.domain.state.*

abstract class NetworkResultHandler<ViewState, Data>(
    private val result: NetworkResult<Data?>,
    private val stateEvent: StateEvent?
) : ResponseHandler<ViewState, Data> {
    override fun getResult(): DataState<ViewState>? {
        return when (result) {
            is NetworkResult.GenericError -> {
                DataState.error(
                    response = Response(
                        message = "${stateEvent?.eventName()}\n\n" +
                                "Reason: ${result.errorMessage}",
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
                if (result.data == null) {
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
                    handleResponse(result.data)
                }
            }
        }
    }
}