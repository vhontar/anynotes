package com.vhontar.anynotes.business.usecases.common

import com.vhontar.anynotes.business.data.cache.CacheErrors
import com.vhontar.anynotes.business.data.cache.FORCE_DELETE_NOTE_EXCEPTION
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.di.DependenciesContainer
import com.vhontar.anynotes.framework.presentation.notelist.state.NoteListStateEvent
import com.vhontar.anynotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

/*
Test cases:
1. deleteNote_success_confirmNetworkUpdated()
    a) delete a note
    b) check for success message from flow emission
    c) confirm note was deleted from "notes" node in network
    d) confirm note was added to "deletes" node in network
2. deleteNote_fail_confirmNetworkUnchanged()
    a) attempt to delete a note, fail since does not exist
    b) check for failure message from flow emission
    c) confirm network was not changed
3. throwException_checkGenericError_confirmNetworkUnchanged()
    a) attempt to delete a note, force an exception to throw
    b) check for failure message from flow emission
    c) confirm network was not changed
 */
class DeleteNoteUseCaseTest {
    // system in test
    private val deletedNote: DeleteNoteUseCase<NoteListViewState>

    // dependencies
    private val dependenciesContainer = DependenciesContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource

    init {
        dependenciesContainer.build()
        noteCacheDataSource = dependenciesContainer.noteCacheDataSource
        noteNetworkDataSource = dependenciesContainer.noteNetworkDataSource
        deletedNote = DeleteNoteUseCase(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun deleteNote_success_confirmNetworkUpdated() = runBlocking {
        val anyNote = noteCacheDataSource.getAllNotes().random()

        deletedNote.deleteNote(
            note = anyNote,
            stateEvent = NoteListStateEvent.DeleteNoteEvent(anyNote)
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                DeleteNoteUseCase.DELETE_NOTE_SUCCESS
            )
        }

        // check whether network is updated
        val networkNotes = noteNetworkDataSource.getAllNotes()

        assertTrue {
            !networkNotes.contains(anyNote)
        }

        // check whether network is updated
        val deletedNotes = noteNetworkDataSource.getDeletedNotes()

        assertTrue {
            deletedNotes.contains(anyNote)
        }
    }

    @Test
    fun deleteNote_fail_confirmNetworkUnchanged() = runBlocking {
        val anyNote = Note(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            createdAt = UUID.randomUUID().toString(),
            updatedAt = UUID.randomUUID().toString()
        )

        deletedNote.deleteNote(
            note = anyNote,
            stateEvent = NoteListStateEvent.DeleteNoteEvent(anyNote)
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                DeleteNoteUseCase.DELETE_NOTE_FAILED
            )
        }

        // check whether network is updated
        val networkNotes = noteNetworkDataSource.getAllNotes().size
        val cacheNotes = noteCacheDataSource.getNumNotes()

        assertTrue {
            networkNotes == cacheNotes
        }

        // check deleted notes
        val deletedNotes = noteNetworkDataSource.getDeletedNotes()

        assertTrue {
            !deletedNotes.contains(anyNote)
        }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkUnchanged() = runBlocking {
        val anyNote = Note(
            id = FORCE_DELETE_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            createdAt = UUID.randomUUID().toString(),
            updatedAt = UUID.randomUUID().toString()
        )

        deletedNote.deleteNote(
            note = anyNote,
            stateEvent = NoteListStateEvent.DeleteNoteEvent(anyNote)
        ).collect {
            assertTrue {
                it?.stateMessage?.response?.message?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
            }
        }

        // check whether network is updated
        val networkNotes = noteNetworkDataSource.getAllNotes().size
        val cacheNotes = noteCacheDataSource.getNumNotes()

        assertTrue {
            networkNotes == cacheNotes
        }

        // check deleted notes
        val deletedNotes = noteNetworkDataSource.getDeletedNotes()

        assertTrue {
            !deletedNotes.contains(anyNote)
        }
    }
}