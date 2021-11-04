package com.vhontar.anynotes.business.usecases.common

import com.vhontar.anynotes.business.data.cache.CacheResultHandler
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.data.util.safeCacheCall
import com.vhontar.anynotes.business.data.util.safeNetworkCall
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.state.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteNoteUseCase<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {
    fun deleteNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<ViewState>?> = flow {
        val cachedResult = safeCacheCall(IO) {
            noteCacheDataSource.deleteNote(note.id)
        }

        val handledCachedResult = object : CacheResultHandler<ViewState, Int>(
            result = cachedResult,
            stateEvent = stateEvent
        ) {
            override fun handleResponse(responseObj: Int): DataState<ViewState> {
                return if (responseObj > 0) {
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()

        emit(handledCachedResult)

        updateNetwork(handledCachedResult?.stateMessage?.response?.message, note)
    }

    private suspend fun updateNetwork(message: String?, note: Note) {
        if (message == DELETE_NOTE_SUCCESS) {
            safeNetworkCall(IO) {
                noteNetworkDataSource.deleteNote(note.id)
            }

            safeNetworkCall(IO) {
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }

    companion object {
        const val DELETE_NOTE_SUCCESS = "Successfully deleted note."
        const val DELETE_NOTE_FAILED = "Failed to delete note."
    }
}