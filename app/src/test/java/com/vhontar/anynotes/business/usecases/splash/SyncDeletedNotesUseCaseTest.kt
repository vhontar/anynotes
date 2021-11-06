package com.vhontar.anynotes.business.usecases.splash

import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.di.DependenciesContainer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
Test cases:
1. deleteNetworkNotes_confirmCacheSync()
    a) select some notes for deleting from network
    b) delete from network
    c) perform sync
    d) confirm notes from cache were deleted
 */
class SyncDeletedNotesUseCaseTest {
    // system in test
    private val syncDeletedNotesUseCase: SyncDeletedNotesUseCase

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
        syncDeletedNotesUseCase = SyncDeletedNotesUseCase(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun deleteNetworkNotes_confirmCacheSync() = runBlocking {
        val notesToDelete = noteNetworkDataSource.getAllNotes().subList(2, 5)

        notesToDelete.forEach {
            noteNetworkDataSource.deleteNote(it.id)
        }

        syncDeletedNotesUseCase.syncDeletedNotes()

        notesToDelete.forEach {
            val cacheNote = noteCacheDataSource.searchNoteById(it.id)
            assertTrue { cacheNote == null }
        }
    }
}