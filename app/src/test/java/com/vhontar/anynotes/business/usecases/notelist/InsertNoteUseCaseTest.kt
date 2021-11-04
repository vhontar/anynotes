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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

/*
Test cases:
1. insertNote_success_confirmNetworkAndCacheUpdated()
    a) insert a new note
    b) listen for INSERT_NOTE_SUCCESS emission from flow
    c) confirm cache was updated with new note
    d) confirm network was updated with new note
2. insertNote_fail_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force a failure (return -1 from db operation)
    c) listen for INSERT_NOTE_FAILED emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force an exception
    c) listen for CACHE_ERROR_UNKNOWN emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
 */
class InsertNoteUseCaseTest {

    // system in test
    private val insertNewNote: InsertNoteUseCase

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
        insertNewNote = InsertNoteUseCase(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource,
            noteFactory = noteFactory
        )
    }

    @Test
    fun insertNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {
        val newNote = noteFactory.createSingleNote(
            id = FORCE_GENERAL_FAILURE,
            title = UUID.randomUUID().toString()
        )

        insertNewNote.insertNote(
            id = newNote.id,
            title = newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(title = newNote.title)
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                InsertNoteUseCase.INSERT_NOTE_FAIL
            )
        }

        // check if cache was updated
        val foundCacheNote = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue(foundCacheNote == null)

        // check if network was updated
        val foundNetworkNote = noteNetworkDataSource.searchNote(newNote)
        assertTrue(foundNetworkNote == null)
    }

    @Test
    fun insertNote_success_confirmNetworkAndCacheUpdated() = runBlocking {
        val newNote = noteFactory.createSingleNote(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString()
        )

        insertNewNote.insertNote(
            id = newNote.id,
            title = newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(title = newNote.title)
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                InsertNoteUseCase.INSERT_NOTE_SUCCESS
            )
        }

        // check if cache was updated
        val foundCacheNote = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue(foundCacheNote == newNote)

        // check if network was updated
        val foundNetworkNote = noteNetworkDataSource.searchNote(newNote)
        assertTrue(foundNetworkNote == newNote)
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {
        val newNote = noteFactory.createSingleNote(
            id = FORCE_NEW_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString()
        )

        insertNewNote.insertNote(
            id = newNote.id,
            title = newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(title = newNote.title)
        ).collect {
            assert(
                it?.stateMessage?.response?.message?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
            )
        }

        // check if cache was updated
        val foundCacheNote = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue(foundCacheNote == null)

        // check if network was updated
        val foundNetworkNote = noteNetworkDataSource.searchNote(newNote)
        assertTrue(foundNetworkNote == null)
    }
}