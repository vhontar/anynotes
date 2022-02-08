package com.vhontar.anynotes.business.usecases.notelist

import com.vhontar.anynotes.business.data.cache.FORCE_DELETE_NOTE_EXCEPTION
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.di.DependenciesContainer
import com.vhontar.anynotes.presentation.notelist.state.NoteListStateEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

/*
Test cases:
1. deleteNotes_success_confirmNetworkAndCacheUpdated()
    a) select a handful of random notes for deleting
    b) delete from cache and network
    c) confirm DELETE_NOTES_SUCCESS msg is emitted from flow
    d) confirm notes are delted from cache
    e) confirm notes are deleted from "notes" node in network
    f) confirm notes are added to "deletes" node in network
2. deleteNotes_fail_confirmCorrectDeletesMade()
    - This is a complex one:
        - The use-case will attempt to delete all notes passed as input. If there
        is an error with a particular delete, it continues with the others. But the
        resulting msg is DELETE_NOTES_ERRORS. So we need to do rigorous checks here
        to make sure the correct notes were deleted and the correct notes were not.
    a) select a handful of random notes for deleting
    b) change the ids of a few notes so they will cause errors when deleting
    c) confirm DELETE_NOTES_ERRORS msg is emitted from flow
    d) confirm ONLY the valid notes are deleted from network "notes" node
    e) confirm ONLY the valid notes are inserted into network "deletes" node
    f) confirm ONLY the valid notes are deleted from cache
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) select a handful of random notes for deleting
    b) force an exception to be thrown on one of them
    c) confirm DELETE_NOTES_ERRORS msg is emitted from flow
    d) confirm ONLY the valid notes are deleted from network "notes" node
    e) confirm ONLY the valid notes are inserted into network "deletes" node
    f) confirm ONLY the valid notes are deleted from cache
 */
class DeleteMultipleNotesUseCaseTest {
    // system in test
    private var deleteMultipleNotes: DeleteMultipleNotesUseCase? = null

    // dependencies
    private lateinit var dependenciesContainer: DependenciesContainer
    private lateinit var noteCacheDataSource: NoteCacheDataSource
    private lateinit var noteNetworkDataSource: NoteNetworkDataSource
    private lateinit var noteFactory: NoteFactory

    @AfterEach
    fun afterEach() {
        deleteMultipleNotes = null
    }

    @BeforeEach
    fun beforeEach() {
        dependenciesContainer = DependenciesContainer()
        dependenciesContainer.build()
        noteCacheDataSource = dependenciesContainer.noteCacheDataSource
        noteNetworkDataSource = dependenciesContainer.noteNetworkDataSource
        noteFactory = dependenciesContainer.noteFactory
        deleteMultipleNotes = DeleteMultipleNotesUseCase(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun deleteNotes_success_confirmNetworkAndCacheUpdated() = runBlocking {
        val randomNotes = noteCacheDataSource.getAllNotes().subList(1, 4)

        deleteMultipleNotes?.deleteNotes(
            notes = randomNotes,
            stateEvent = NoteListStateEvent.DeleteMultipleNotesEvent(randomNotes)
        )?.collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                DeleteMultipleNotesUseCase.DELETE_NOTES_SUCCESS
            )
        }

        // confirm that notes were inserted inside 'deleted' table
        val deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue { deletedNotes.containsAll(randomNotes) }

        // confirm that notes were deleted from 'notes' table
        val networkNotes = noteNetworkDataSource.getAllNotes()
        assertFalse { networkNotes.containsAll(randomNotes) }

        // confirm that notes were deleted from cache
        randomNotes.forEach {
            val searchedNote = noteCacheDataSource.searchNoteById(it.id)
            assertTrue { searchedNote == null }
        }
    }

    @Test
    fun deleteNotes_fail_confirmCorrectDeletesMade() = runBlocking {
        val oldNumNotesInCache = noteCacheDataSource.getNumNotes()
        val validNotes = noteCacheDataSource.getAllNotes().subList(1, 4)
        val foundNote = noteCacheDataSource.getAllNotes()[6]
        val notValidNote = noteFactory.createSingleNote(
            id = UUID.randomUUID().toString(),
            title = foundNote.title,
            body = foundNote.body
        )

        val allNotes = arrayListOf(notValidNote)
        allNotes.addAll(validNotes)

        deleteMultipleNotes?.deleteNotes(
            notes = allNotes,
            stateEvent = NoteListStateEvent.DeleteMultipleNotesEvent(allNotes)
        )?.collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                DeleteMultipleNotesUseCase.DELETE_NOTES_ERRORS
            )
        }

        // confirm that valid notes were deleted from 'notes' table, but not non-valid
        val networkNotes = noteNetworkDataSource.getAllNotes()
        assertFalse { networkNotes.containsAll(validNotes) }
        // assertTrue { networkNotes.contains(notValidNote) }

        // confirm that valid notes were added to 'deleted' table, but not non-valid
        val deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue { deletedNotes.containsAll(validNotes) }
        assertFalse { deletedNotes.contains(notValidNote) }

        // confirm that valid notes were deleted from cache, but not non-valid
        validNotes.forEach {
            val cacheNote = noteCacheDataSource.searchNoteById(it.id)
            assertTrue { cacheNote == null }
        }
        val numNotes = noteCacheDataSource.getNumNotes()
        assertTrue { numNotes == (oldNumNotesInCache - validNotes.size) }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {
        val oldNumNotesInCache = noteCacheDataSource.getNumNotes()
        val validNotes = noteCacheDataSource.getAllNotes().subList(1, 4)

        val notValidNote = noteFactory.createSingleNote(
            id = FORCE_DELETE_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )

        val allNotes = arrayListOf(notValidNote)
        allNotes.addAll(validNotes)

        deleteMultipleNotes?.deleteNotes(
            notes = allNotes,
            stateEvent = NoteListStateEvent.DeleteMultipleNotesEvent(allNotes)
        )?.collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                DeleteMultipleNotesUseCase.DELETE_NOTES_ERRORS
            )
        }

        // confirm that valid notes were deleted from 'notes' table, but not non-valid
        val networkNotes = noteNetworkDataSource.getAllNotes()
        assertFalse { networkNotes.containsAll(validNotes) }
        // assertTrue { networkNotes.contains(notValidNote) }

        // confirm that valid notes were added to 'deleted' table, but not non-valid
        val deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue { deletedNotes.containsAll(validNotes) }
        assertFalse { deletedNotes.contains(notValidNote) }

        // confirm that valid notes were deleted from cache, but not non-valid
        validNotes.forEach {
            val cacheNote = noteCacheDataSource.searchNoteById(it.id)
            assertTrue { cacheNote == null }
        }
        val numNotes = noteCacheDataSource.getNumNotes()
        assertTrue { numNotes == (oldNumNotesInCache - validNotes.size) }
    }
}