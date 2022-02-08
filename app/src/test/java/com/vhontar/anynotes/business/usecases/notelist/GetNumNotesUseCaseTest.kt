package com.vhontar.anynotes.business.usecases.notelist

import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.di.DependenciesContainer
import com.vhontar.anynotes.presentation.notelist.state.NoteListStateEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetNumNotesUseCaseTest {
    // system in test
    private val getNumNotesUseCase: GetNumNotesUseCase

    // dependencies
    private val dependenciesContainer = DependenciesContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteFactory: NoteFactory

    init {
        dependenciesContainer.build()
        noteCacheDataSource = dependenciesContainer.noteCacheDataSource
        noteFactory = dependenciesContainer.noteFactory
        getNumNotesUseCase = GetNumNotesUseCase(
            noteCacheDataSource = noteCacheDataSource
        )
    }

    @Test
    fun getNumNotes_success_confirmCorrect() = runBlocking {
        var numNotes = 0
        getNumNotesUseCase.getNumNotes(
            stateEvent = NoteListStateEvent.GetNumNotesInCacheEvent
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                GetNumNotesUseCase.GET_NUM_NOTES_SUCCESS
            )

            numNotes = it?.data?.numNotesInCache ?: 0
        }

        val actualNumNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue {
            numNotes == actualNumNotesInCache
        }
    }
}