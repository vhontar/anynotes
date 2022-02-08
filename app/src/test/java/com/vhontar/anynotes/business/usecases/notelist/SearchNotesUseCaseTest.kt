package com.vhontar.anynotes.business.usecases.notelist

import com.vhontar.anynotes.business.data.cache.CacheErrors
import com.vhontar.anynotes.business.data.cache.FORCE_SEARCH_NOTES_EXCEPTION
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.di.DependenciesContainer
import com.vhontar.anynotes.datasource.cache.database.ORDER_BY_ASC_DATE_UPDATED
import com.vhontar.anynotes.presentation.notelist.state.NoteListStateEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
Test cases:
1. blankQuery_success_confirmNotesRetrieved()
    a) query with some default search options
    b) listen for SEARCH_NOTES_SUCCESS emitted from flow
    c) confirm notes were retrieved
    d) confirm notes in cache match with notes that were retrieved
2. randomQuery_success_confirmNoResults()
    a) query with something that will yield no results
    b) listen for SEARCH_NOTES_NO_MATCHING_RESULTS emitted from flow
    c) confirm nothing was retrieved
    d) confirm there is notes in the cache
3. searchNotes_fail_confirmNoResults()
    a) force an exception to be thrown
    b) listen for CACHE_ERROR_UNKNOWN emitted from flow
    c) confirm nothing was retrieved
    d) confirm there is notes in the cache
 */
class SearchNotesUseCaseTest {
    // system in test
    private val searchNotesUseCase: SearchNotesUseCase

    // dependencies
    private val dependenciesContainer = DependenciesContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteFactory: NoteFactory

    init {
        dependenciesContainer.build()
        noteCacheDataSource = dependenciesContainer.noteCacheDataSource
        noteFactory = dependenciesContainer.noteFactory
        searchNotesUseCase = SearchNotesUseCase(
            noteCacheDataSource = noteCacheDataSource
        )
    }

    @Test
    fun blankQuery_success_confirmNotesRetrieved() = runBlocking {
        val query = ""
        var results: List<Note>? = null
        searchNotesUseCase.searchNotes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = NoteListStateEvent.SearchNotesEvent()
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.message,
                SearchNotesUseCase.SEARCH_NOTES_SUCCESS
            )

            results = it?.data?.noteList
        }

        // confirm that search list is not null
        assertTrue {
            results != null
        }

        // confirm that cache result is the same
        val notesInCache = noteCacheDataSource.searchNotes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue {
            results?.containsAll(notesInCache) ?: false
        }
    }

    @Test
    fun randomQuery_success_confirmNoResults() = runBlocking {
        val query = "asdfghj"
        var results: List<Note>? = null
        searchNotesUseCase.searchNotes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = NoteListStateEvent.SearchNotesEvent()
        ).collect {
            assertTrue {
                it?.stateMessage?.response?.message == SearchNotesUseCase.SEARCH_NOTES_NO_MATCHING_RESULTS
            }

            results = it?.data?.noteList
        }

        // confirm that list is empty
        assertTrue {
            results.isNullOrEmpty()
        }

        // confirm there is notes in the cache
        val notesInCache = noteCacheDataSource.searchNotes(
            query = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue {
            notesInCache.isNotEmpty()
        }
    }

    @Test
    fun searchNotes_fail_confirmNoResults() = runBlocking {
        val query = FORCE_SEARCH_NOTES_EXCEPTION
        var results: List<Note>? = null

        searchNotesUseCase.searchNotes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = NoteListStateEvent.SearchNotesEvent()
        ).collect {
            assertTrue {
                it?.stateMessage?.response?.message?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
            }

            results = it?.data?.noteList
        }

        // confirm that list is empty
        assertTrue {
            results.isNullOrEmpty()
        }

        // confirm there is notes in the cache
        val notesInCache = noteCacheDataSource.searchNotes(
            query = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue {
            notesInCache.isNotEmpty()
        }
    }
}