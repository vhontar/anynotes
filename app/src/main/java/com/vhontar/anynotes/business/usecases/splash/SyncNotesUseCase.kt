package com.vhontar.anynotes.business.usecases.splash

import com.vhontar.anynotes.business.data.cache.CacheResultHandler
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.NetworkResultHandler
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.data.util.safeCacheCall
import com.vhontar.anynotes.business.data.util.safeNetworkCall
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.state.DataState
import com.vhontar.anynotes.util.printLogD
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class SyncNotesUseCase(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    suspend fun syncNotes() {
        val cacheNotes = getCacheNotes()
        val networkNotes = getNetworkNotes()

        syncNetworkNotesWithCacheNotes(cacheNotes, networkNotes)
    }

    private suspend fun getCacheNotes(): List<Note> {
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.getAllNotes()
        }

        val handledCacheResult = object : CacheResultHandler<List<Note>, List<Note>>(
            result = cacheResult,
            stateEvent = null
        ) {
            override fun handleResponse(responseObj: List<Note>): DataState<List<Note>> {
                return DataState.data(
                    response = null,
                    data = responseObj,
                    null
                )
            }
        }.getResult()

        return handledCacheResult?.data ?: arrayListOf()
    }

    private suspend fun getNetworkNotes(): List<Note> {
        val networkResult = safeNetworkCall(IO) {
            noteNetworkDataSource.getAllNotes()
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

        return handledNetworkResult?.data ?: arrayListOf()
    }

    // if they do not exist in cache, insert them
    // if they do exist in cache, make sure they are up-to-date
    // while looping, remove notes from the cachedNotes list. If any remain, it means they
    // should be in the network but aren't. So insert them.
    private suspend fun syncNetworkNotesWithCacheNotes(
        cacheNotes: List<Note>,
        networkNotes: List<Note>
    ) = withContext(IO) {
        cacheNotes as ArrayList

        networkNotes.forEach { networkNote ->
            noteCacheDataSource.searchNoteById(networkNote.id)?.let { cacheNote ->
                cacheNotes.remove(cacheNote)
                checkIfCachedNoteRequiresUpdate(cacheNote, networkNote)
            } ?: noteCacheDataSource.insertNote(networkNote)
        }

        cacheNotes.forEach { cacheNote ->
            safeNetworkCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(cacheNote)
            }
        }
    }

    private suspend fun checkIfCachedNoteRequiresUpdate(
        cacheNote: Note,
        networkNote: Note
    ) {
        if (networkNote.updatedAt > cacheNote.updatedAt) {
            safeCacheCall(IO) {
                noteCacheDataSource.updateNote(
                    primaryKey = networkNote.id,
                    newTitle = networkNote.title,
                    newBody = networkNote.body,
                    timestamp = networkNote.updatedAt
                )
            }
        } else if (networkNote.updatedAt < cacheNote.updatedAt) {
            safeNetworkCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(cacheNote)
            }
        }
    }
}