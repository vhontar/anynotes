package com.vhontar.anynotes.business.usecases.notelist

import com.vhontar.anynotes.business.data.cache.CacheResultHandler
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.util.safeCacheCall
import com.vhontar.anynotes.business.domain.state.*
import com.vhontar.anynotes.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetNumNotesUseCase(
    private val noteCacheDataSource: NoteCacheDataSource
) {

    fun getNumNotes(
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.getNumNotes()
        }

        val handledCacheResult = object : CacheResultHandler<NoteListViewState, Int>(
            result = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleResponse(responseObj: Int): DataState<NoteListViewState> {
                return DataState.data(
                    response = Response(
                        message = GET_NUM_NOTES_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = NoteListViewState(numNotesInCache = responseObj),
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(handledCacheResult)
    }

    companion object{
        val GET_NUM_NOTES_SUCCESS = "Successfully retrieved the number of notes from the cache."
        val GET_NUM_NOTES_FAILED = "Failed to get the number of notes from the cache."
    }
}