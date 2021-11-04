package com.vhontar.anynotes.business.usecases.splash

import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.di.DependenciesContainer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

/*
Test cases:
1. insertNetworkNotesIntoCache()
    a) insert a bunch of new notes into the cache
    b) perform the sync
    c) check to see that those notes were inserted into the network
2. insertCachedNotesIntoNetwork()
    a) insert a bunch of new notes into the network
    b) perform the sync
    c) check to see that those notes were inserted into the cache
3. checkCacheUpdateLogicSync()
    a) select some notes from the cache and update them
    b) perform sync
    c) confirm network reflects the updates
4. checkNetworkUpdateLogicSync()
    a) select some notes from the network and update them
    b) perform sync
    c) confirm cache reflects the updates
 */
class SyncNotesUseCaseTest {
    // system in test
    private val syncNotesUseCase: SyncNotesUseCase

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
        syncNotesUseCase = SyncNotesUseCase(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun insertNetworkNotesIntoCache() = runBlocking {
        val notes = noteFactory.createNoteList(50)
        noteNetworkDataSource.insertOrUpdateNotes(notes)

        syncNotesUseCase.syncNotes()

        notes.forEach {
            val cacheNote = noteCacheDataSource.searchNoteById(it.id)
            assertTrue { cacheNote != null }
        }
    }

    @Test
    fun insertCachedNotesIntoNetwork() = runBlocking {
        val notes = noteFactory.createNoteList(50)
        noteCacheDataSource.insertNotes(notes)

        syncNotesUseCase.syncNotes()

        notes.forEach {
            val networkNote = noteNetworkDataSource.searchNote(it)
            assertTrue { networkNote != null }
        }
    }

    @Test
    fun checkCacheUpdateLogicSync() = runBlocking {
        val cacheNote = noteCacheDataSource.getAllNotes().random()
        val updatedNote = noteFactory.createSingleNote(
            id = cacheNote.id,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )

        noteCacheDataSource.insertNote(updatedNote)

        syncNotesUseCase.syncNotes()

        val networkUpdatedNote = noteNetworkDataSource.searchNote(updatedNote)
        assertEquals(updatedNote.id, networkUpdatedNote?.id)
        assertEquals(updatedNote.title, networkUpdatedNote?.title)
        assertEquals(updatedNote.body, networkUpdatedNote?.body)
        assertEquals(updatedNote.updatedAt, networkUpdatedNote?.updatedAt)
    }

    @Test
    fun checkNetworkUpdateLogicSync() = runBlocking {
        val cacheNote = noteNetworkDataSource.getAllNotes().random()
        val updatedNote = noteFactory.createSingleNote(
            id = cacheNote.id,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )

        noteNetworkDataSource.insertOrUpdateNote(updatedNote)

        syncNotesUseCase.syncNotes()

        val cacheUpdatedNote = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertEquals(updatedNote.id, cacheUpdatedNote?.id)
        assertEquals(updatedNote.title, cacheUpdatedNote?.title)
        assertEquals(updatedNote.body, cacheUpdatedNote?.body)
        assertEquals(updatedNote.updatedAt, cacheUpdatedNote?.updatedAt)
    }
}