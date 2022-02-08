package com.vhontar.anynotes.business.usecases.notelist

import com.vhontar.anynotes.business.data.cache.CacheResultHandler
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.data.util.safeCacheCall
import com.vhontar.anynotes.business.data.util.safeNetworkCall
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.business.domain.state.*
import com.vhontar.anynotes.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class InsertNoteUseCase(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val noteFactory: NoteFactory
) {
    fun insertNote(
        id: String?,
        title: String,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        val newNote = noteFactory.createSingleNote(
            id = id,
            title = title,
            body = ""
        )

        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.insertNote(newNote)
        }

        val cacheDateState = object : CacheResultHandler<NoteListViewState, Long>(
            result = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleResponse(responseObj: Long): DataState<NoteListViewState> {
                return if (responseObj > 0) {
                    val noteViewState = NoteListViewState(
                        newNote = newNote
                    )

                    DataState.data(
                        response = Response(
                            message = INSERT_NOTE_SUCCESS,
                            messageType = MessageType.Success,
                            uiComponentType = UIComponentType.Toast
                        ),
                        noteViewState,
                        stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = INSERT_NOTE_FAIL,
                            messageType = MessageType.Error,
                            uiComponentType = UIComponentType.Toast
                        ),
                        null,
                        stateEvent
                    )
                }
            }
        }.getResult()

        emit(cacheDateState)

        // insert note to network
        insertNoteApi(
            message = cacheDateState?.stateMessage?.response?.message,
            newNote = newNote
        )
    }

    private suspend fun insertNoteApi(message: String?, newNote: Note) {
        if (message == INSERT_NOTE_SUCCESS) {
            safeNetworkCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(newNote)
            }
        }
    }

    companion object {
        const val INSERT_NOTE_SUCCESS = "Successfully inserted a note."
        const val INSERT_NOTE_FAIL = "Failed to insert a note."
    }
}