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

class DeleteMultipleNotesUseCase(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {
    private var onDeleteError: Boolean = false

    fun deleteNotes(
        notes: List<Note>,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        val successfulDeletes = arrayListOf<Note>()

        notes.forEach {
            val cacheResult = safeCacheCall(IO) {
                noteCacheDataSource.deleteNote(it.id)
            }

            val handledCachedResult = object : CacheResultHandler<NoteListViewState, Int>(
                result = cacheResult,
                stateEvent = stateEvent
            ) {
                override fun handleResponse(responseObj: Int): DataState<NoteListViewState>? {
                    if (responseObj >= 0)
                        successfulDeletes.add(it)
                    else
                        onDeleteError = true

                    return null
                }
            }.getResult()

            if (handledCachedResult?.stateMessage?.response?.message?.contains(stateEvent.errorInfo()) == true) {
                onDeleteError = true
            }
        }

        val dataState = if (onDeleteError) {
            DataState.data<NoteListViewState>(
                response = Response(
                    message = DELETE_NOTES_ERRORS,
                    uiComponentType = UIComponentType.Dialog,
                    messageType = MessageType.Error
                ),
                data = null,
                stateEvent = stateEvent
            )
        } else {
            DataState.data<NoteListViewState>(
                response = Response(
                    message = DELETE_NOTES_SUCCESS,
                    uiComponentType = UIComponentType.Dialog,
                    messageType = MessageType.Error
                ),
                data = null,
                stateEvent = stateEvent
            )
        }

        emit(dataState)

        updateNetwork(successfulDeletes)
    }

    private suspend fun updateNetwork(successfulDeletes: List<Note>) {
        successfulDeletes.forEach {

            // delete from notes
            safeNetworkCall(IO) {
                noteNetworkDataSource.deleteNote(primaryKey = it.id)
            }

            // add to deleted notes
            safeNetworkCall(IO) {
                noteNetworkDataSource.insertDeletedNote(note = it)
            }
        }
    }

    companion object{
        val DELETE_NOTES_SUCCESS = "Successfully deleted notes."
        val DELETE_NOTES_ERRORS = "Not all the notes you selected were deleted. There was some errors."
        val DELETE_NOTES_YOU_MUST_SELECT = "You haven't selected any notes to delete."
        val DELETE_NOTES_ARE_YOU_SURE = "Are you sure you want to delete these?"
    }
}