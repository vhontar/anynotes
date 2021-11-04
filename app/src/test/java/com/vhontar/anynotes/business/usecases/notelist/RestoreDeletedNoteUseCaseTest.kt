package com.vhontar.anynotes.business.usecases.notelist

import com.vhontar.anynotes.business.data.cache.CacheErrors
import com.vhontar.anynotes.business.data.cache.FORCE_GENERAL_FAILURE
import com.vhontar.anynotes.business.data.cache.FORCE_NEW_NOTE_EXCEPTION
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.di.DependenciesContainer
import com.vhontar.anynotes.framework.presentation.notelist.state.NoteListStateEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

/*
Test cases:
1. restoreNote_success_confirmCacheAndNetworkUpdated()
    a) create a new note and insert it into the "deleted" node of network
    b) restore that note
    c) Listen for success msg RESTORE_NOTE_SUCCESS from flow
    d) confirm note is in the cache
    e) confirm note is in the network "notes" node
    f) confirm note is not in the network "deletes" node
2. restoreNote_fail_confirmCacheAndNetworkUnchanged()
    a) create a new note and insert it into the "deleted" node of network
    b) restore that note (force a failure)
    c) Listen for success msg RESTORE_NOTE_FAILED from flow
    d) confirm note is not in the cache
    e) confirm note is not in the network "notes" node
    f) confirm note is in the network "deletes" node
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) create a new note and insert it into the "deleted" node of network
    b) restore that note (force an exception)
    c) Listen for success msg CACHE_ERROR_UNKNOWN from flow
    d) confirm note is not in the cache
    e) confirm note is not in the network "notes" node
    f) confirm note is in the network "deletes" node
 */
class RestoreDeletedNoteUseCaseTest {
    // system in test
    private val restoreDeletedNoteUseCase: RestoreDeletedNoteUseCase

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
        restoreDeletedNoteUseCase = RestoreDeletedNoteUseCase(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun restoreNote_success_confirmCacheAndNetworkUpdated() = runBlocking {
        val restoredNote = noteFactory.createSingleNote(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )
        noteNetworkDataSource.insertDeletedNote(restoredNote)

        restoreDeletedNoteUseCase.restoreDeletedNotes(
            note = restoredNote,
            stateEvent = NoteListStateEvent.RestoreDeletedNoteEvent(restoredNote)
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                RestoreDeletedNoteUseCase.RESTORE_NOTE_SUCCESS
            )
        }

        // confirm that note is in the network 'notes' table
        val networkNote = noteNetworkDataSource.searchNote(restoredNote)
        assertTrue { networkNote == restoredNote }

        // confirm that note is in the cache
        val cacheNote = noteCacheDataSource.searchNoteById(restoredNote.id)
        assertTrue { cacheNote == restoredNote }

        // confirm that note is NOT in the network 'deleted' table
        val deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertFalse { deletedNotes.contains(restoredNote) }
    }

    @Test
    fun restoreNote_fail_confirmCacheAndNetworkUnchanged() = runBlocking {
        val restoredNote = noteFactory.createSingleNote(
            id = FORCE_GENERAL_FAILURE,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )
        noteNetworkDataSource.insertDeletedNote(restoredNote)

        restoreDeletedNoteUseCase.restoreDeletedNotes(
            note = restoredNote,
            stateEvent = NoteListStateEvent.RestoreDeletedNoteEvent(restoredNote)
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                RestoreDeletedNoteUseCase.RESTORE_NOTE_FAILED
            )
        }

        // confirm that note is NOT in the network 'notes' table
        val networkNote = noteNetworkDataSource.searchNote(restoredNote)
        assertFalse { networkNote == restoredNote }

        // confirm that note is NOT in the cache
        val cacheNote = noteCacheDataSource.searchNoteById(restoredNote.id)
        assertFalse { cacheNote == restoredNote }

        // confirm that note is in the network 'deleted' table
        val deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue  { deletedNotes.contains(restoredNote) }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {
        val restoredNote = noteFactory.createSingleNote(
            id = FORCE_NEW_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )
        noteNetworkDataSource.insertDeletedNote(restoredNote)

        restoreDeletedNoteUseCase.restoreDeletedNotes(
            note = restoredNote,
            stateEvent = NoteListStateEvent.RestoreDeletedNoteEvent(restoredNote)
        ).collect {
            assert(
                it?.stateMessage?.response?.message?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
            )
        }

        // confirm that note is NOT in the network 'notes' table
        val networkNote = noteNetworkDataSource.searchNote(restoredNote)
        assertFalse { networkNote == restoredNote }

        // confirm that note is NOT in the cache
        val cacheNote = noteCacheDataSource.searchNoteById(restoredNote.id)
        assertFalse { cacheNote == restoredNote }

        // confirm that note is in the network 'deleted' table
        val deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue  { deletedNotes.contains(restoredNote) }
    }
}