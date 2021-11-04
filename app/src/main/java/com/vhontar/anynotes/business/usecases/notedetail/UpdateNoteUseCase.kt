package com.vhontar.anynotes.business.usecases.notedetail

import com.vhontar.anynotes.business.data.cache.CacheResultHandler
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.data.util.safeCacheCall
import com.vhontar.anynotes.business.data.util.safeNetworkCall
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.state.*
import com.vhontar.anynotes.framework.presentation.notedetail.state.NoteDetailViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateNoteUseCase(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {
    fun updateNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<NoteDetailViewState>?> = flow {

        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.updateNote(
                primaryKey = note.id,
                newTitle = note.title,
                newBody = note.body
            )
        }

        val handledCacheResult = object : CacheResultHandler<NoteDetailViewState, Long>(
            result = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleResponse(responseObj: Long): DataState<NoteDetailViewState>? {
                return if (responseObj < 0) {
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()

        emit(handledCacheResult)

        updateNetwork(handledCacheResult?.stateMessage?.response?.message, note)
    }

    private suspend fun updateNetwork(message: String?, note: Note) {
        if (message == UPDATE_NOTE_SUCCESS) {
            safeNetworkCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(note)
            }
        }
    }

    companion object {
        val UPDATE_NOTE_SUCCESS = "Successfully updated note."
        val UPDATE_NOTE_FAILED = "Failed to update note."
        val UPDATE_NOTE_FAILED_PK = "Update failed. Note is missing primary key."
    }
}