package com.vhontar.anynotes.business.usecases.splash

import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.NetworkResultHandler
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.data.util.safeCacheCall
import com.vhontar.anynotes.business.data.util.safeNetworkCall
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.state.DataState
import kotlinx.coroutines.Dispatchers.IO

class SyncDeletedNotesUseCase(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    suspend fun syncDeletedNotes() {
        val networkResult = safeNetworkCall(IO) {
            noteNetworkDataSource.getDeletedNotes()
        }

        val handledNetworkResult = object : NetworkResultHandler<List<Note>, List<Note>>(
            result = networkResult,
            stateEvent = null
        ) {
            override fun handleResponse(responseObj: List<Note>): DataState<List<Note>> {
                return DataState.data(
                    response = null,
                    data = responseObj,
                    stateEvent = null
                )
            }
        }.getResult()

        val deletedNotes = handledNetworkResult?.data ?: arrayListOf()
        safeCacheCall(IO) {
            noteCacheDataSource.deleteNotes(deletedNotes)
        }
    }
}