package com.vhontar.anynotes.business.usecases.notelist

import com.vhontar.anynotes.business.data.cache.CacheResultHandler
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.data.util.safeCacheCall
import com.vhontar.anynotes.business.data.util.safeNetworkCall
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.state.*
import com.vhontar.anynotes.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RestoreDeletedNoteUseCase(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {
    fun restoreDeletedNotes(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.insertNote(note)
        }

        val handledCacheResult = object : CacheResultHandler<NoteListViewState, Long>(
            result = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleResponse(responseObj: Long): DataState<NoteListViewState>? {
                return if (responseObj >= 0) {
                    val viewState = NoteListViewState(
                        notePendingDelete = NoteListViewState.NotePendingDelete(
                            note = note
                        )
                    )

                    DataState.data(
                        response = Response(
                            message = RESTORE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
                        ),
                        data = viewState,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = RESTORE_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
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
        if (message == RESTORE_NOTE_SUCCESS) {
            // network call to delete note from 'deleted' table
            safeNetworkCall(IO) {
                noteNetworkDataSource.deleteDeletedNote(note)
            }

            // network call to insert note into 'notes' table
            safeNetworkCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(note)
            }
        }
    }

    companion object {
        val RESTORE_NOTE_SUCCESS = "Successfully restored the deleted note."
        val RESTORE_NOTE_FAILED = "Failed to restore the deleted note."

    }
}