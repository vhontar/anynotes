package com.vhontar.anynotes.business.usecases.notedetail

import com.vhontar.anynotes.business.data.cache.CacheErrors
import com.vhontar.anynotes.business.data.cache.FORCE_UPDATE_NOTE_EXCEPTION
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.di.DependenciesContainer
import com.vhontar.anynotes.presentation.notedetail.state.NoteDetailStateEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

/*
Test cases:
1. updateNote_success_confirmNetworkAndCacheUpdated()
    a) select a random note from the cache
    b) update that note
    c) confirm UPDATE_NOTE_SUCCESS msg is emitted from flow
    d) confirm note is updated in network
    e) confirm note is updated in cache
2. updateNote_fail_confirmNetworkAndCacheUnchanged()
    a) attempt to update a note, fail since does not exist
    b) check for failure message from flow emission
    c) confirm nothing was updated in the cache
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) attempt to update a note, force an exception to throw
    b) check for failure message from flow emission
    c) confirm nothing was updated in the cache
 */
class UpdateNoteUseCaseTest {
    // system in test
    private val updateNoteUseCase: UpdateNoteUseCase

    // dependencies
    private val dependenciesContainer = DependenciesContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory

    init {
        dependenciesContainer.build()
        noteCacheDataSource = dependenciesContainer.noteCacheDataSource
        noteNetworkDataSource = dependenciesContainer.noteNetworkDataSource
        noteFactory = dependenciesContainer.noteFactory
        updateNoteUseCase = UpdateNoteUseCase(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun updateNote_success_confirmNetworkAndCacheUpdated() = runBlocking {
        val cacheNote = noteCacheDataSource.getAllNotes().random()

        val updatedNote = noteFactory.createSingleNote(
            id = cacheNote.id,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )

        updateNoteUseCase.updateNote(
            note = updatedNote,
            stateEvent = NoteDetailStateEvent.UpdateNoteEvent
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                UpdateNoteUseCase.UPDATE_NOTE_SUCCESS
            )
        }

        // confirm that cache note was updated
        val noteInCache = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertTrue { noteInCache == updatedNote }

        // confirm that network note was updated
        val noteInNetwork = noteNetworkDataSource.searchNote(updatedNote)
        assertTrue { noteInNetwork == updatedNote }
    }

    @Test
    fun updateNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {
        val updatedNote = noteFactory.createSingleNote(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )

        updateNoteUseCase.updateNote(
            note = updatedNote,
            stateEvent = NoteDetailStateEvent.UpdateNoteEvent
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                UpdateNoteUseCase.UPDATE_NOTE_FAILED
            )
        }

        // confirm that cache note was NOT updated
        val noteInCache = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertTrue { noteInCache == null }

        // confirm that network note was NOT updated
        val noteInNetwork = noteNetworkDataSource.searchNote(updatedNote)
        assertTrue { noteInNetwork == null }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {
        val updatedNote = noteFactory.createSingleNote(
            id = FORCE_UPDATE_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )

        updateNoteUseCase.updateNote(
            note = updatedNote,
            stateEvent = NoteDetailStateEvent.UpdateNoteEvent
        ).collect {
            assert(
                it?.stateMessage?.response?.message?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
            )
        }

        // confirm that cache note was NOT updated
        val noteInCache = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertTrue { noteInCache == null }

        // confirm that network note was NOT updated
        val noteInNetwork = noteNetworkDataSource.searchNote(updatedNote)
        assertTrue { noteInNetwork == null }
    }
}