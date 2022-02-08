package com.vhontar.anynotes.business.usecases.notelist

import com.vhontar.anynotes.business.data.cache.CacheResultHandler
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.util.safeCacheCall
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.state.*
import com.vhontar.anynotes.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchNotesUseCase(
    private val noteCacheDataSource: NoteCacheDataSource
) {
    fun searchNotes(
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        val updatedPage = if (page < 0) 1 else page

        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.searchNotes(
                query = query,
                filterAndOrder = filterAndOrder,
                page = updatedPage
            )
        }

        val handledCacheResult = object : CacheResultHandler<NoteListViewState, List<Note>>(
            result = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleResponse(responseObj: List<Note>): DataState<NoteListViewState> {
                var message = SEARCH_NOTES_SUCCESS
                var uiComponentType: UIComponentType = UIComponentType.None

                if (responseObj.isEmpty()) {
                    message = SEARCH_NOTES_NO_MATCHING_RESULTS
                    uiComponentType = UIComponentType.Toast
                }

                return DataState.data(
                    response = Response(
                        message = message,
                        uiComponentType = uiComponentType,
                        messageType = MessageType.Success
                    ),
                    data = NoteListViewState(noteList = responseObj as ArrayList<Note>),
                    stateEvent = stateEvent
                )
            }

        }.getResult()

        emit(handledCacheResult)
    }

    companion object{
        val SEARCH_NOTES_SUCCESS = "Successfully retrieved list of notes."
        val SEARCH_NOTES_NO_MATCHING_RESULTS = "There are no notes that match that query."
        val SEARCH_NOTES_FAILED = "Failed to retrieve the list of notes."
    }
}