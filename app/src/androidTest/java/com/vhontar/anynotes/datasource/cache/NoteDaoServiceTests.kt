package com.vhontar.anynotes.datasource.cache

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.vhontar.anynotes.BaseTest
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.datasource.cache.abstraction.NoteDaoService
import com.vhontar.anynotes.datasource.cache.database.NoteDao
import com.vhontar.anynotes.datasource.cache.implemetation.NoteDaoServiceImpl
import com.vhontar.anynotes.datasource.data.NoteDataFactory
import com.vhontar.anynotes.di.TestAppComponent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
    LEGEND:
    1. CBS = "Confirm by searching"
    Test cases:
    1. confirm database note empty to start (should be test data inserted from CacheTest.kt)
    2. insert a new note, CBS
    3. insert a list of notes, CBS
    4. insert 1000 new notes, confirm filtered search query works correctly
    5. insert 1000 new notes, confirm db size increased
    6. delete new note, confirm deleted
    7. delete list of notes, CBS
    8. update a note, confirm updated
    9. search notes, order by date (ASC), confirm order
    10. search notes, order by date (DESC), confirm order
    11. search notes, order by title (ASC), confirm order
    12. search notes, order by title (DESC), confirm order
 */
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class NoteDaoServiceTests: BaseTest() {
    // system in test
    private val noteDaoService: NoteDaoService

    // dependencies
    @Inject
    lateinit var noteDao: NoteDao
    @Inject
    lateinit var noteDataFactory: NoteDataFactory

    init {
        injectTest()
        insertTestData()
        noteDaoService = NoteDaoServiceImpl(
            noteDao = noteDao
        )
    }

    private fun insertTestData() = runBlocking {
        noteDaoService.insertNotes(noteDataFactory.produceListOfNotes())
    }

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }

    @Test
    fun a_searchNotes_confirmDBNoteEmpty() = runBlocking {
        val numNotes = noteDaoService.getNumNotes()
        assertTrue { numNotes > 0}
    }

    @Test
    fun insert_CBS() = runBlocking {
        val newNote = noteDataFactory.createSingleNote(
            id = null,
            title = "super cool title",
            body = "some super body"
        )

        noteDaoService.insertNote(newNote)

        val allNotes = noteDaoService.getAllNotes()
        assertTrue { allNotes.contains(newNote) }
    }

    @Test
    fun insertNoteList_CBS() = runBlocking {
        val noteList = noteDataFactory.createNoteLists(10)
        noteDaoService.insertNotes(noteList)

        val allNotes = noteDaoService.getAllNotes()
        assertTrue { allNotes.containsAll(noteList) }
    }

    @Test
    fun insert1000Notes_searchNotesByTitle_confirm50ExpectedValues() = runBlocking {
        val noteList = noteDataFactory.createNoteLists(1000)
        noteDaoService.insertNotes(noteList)

        repeat(50) {
            val random = Random.nextInt(0, noteList.size - 1)
            val result = noteDaoService.searchNotesOrderByTitleASC(
                query = noteList[random].title,
                page = 1
            )
            assertEquals(
                noteList[random].title,
                result[0].title
            )
        }
    }

    @Test
    fun insert1000Notes_confirmNumNotesInDB() = runBlocking {
        val oldNumNotes = noteDaoService.getNumNotes()
        val noteList = noteDataFactory.createNoteLists(1000)
        noteDaoService.insertNotes(noteList)

        val newNumNotes = noteDaoService.getNumNotes()
        assertTrue { oldNumNotes == newNumNotes }
    }

    @Test
    fun insertNote_deleteNote_confirmedDeleted() = runBlocking {
        val newNote = noteDataFactory.createSingleNote(
            id = null,
            title = "super cool",
            body = "test"
        )
        noteDaoService.insertNote(newNote)

        var allNotes = noteDaoService.searchNotes()
        assertTrue { allNotes.contains(newNote) }

        noteDaoService.deleteNote(newNote.id)
        allNotes = noteDaoService.searchNotes()
        assertFalse { allNotes.contains(newNote) }
    }

    @Test
    fun deleteNotes_confirmedDeleted() = runBlocking {
        val noteList = noteDaoService.getAllNotes() as ArrayList
        val notesToDelete = arrayListOf<Note>()

        var noteToDelete = noteList.random()
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        noteToDelete = noteList.random()
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        noteToDelete = noteList.random()
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        noteToDelete = noteList.random()
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        noteDaoService.deleteNotes(notesToDelete)

        val allNotes = noteDaoService.getAllNotes()
        assertFalse { allNotes.containsAll(notesToDelete) }
    }

    @Test
    fun insertNote_updateNote_confirmUpdated() = runBlocking {
        val newNote = noteDataFactory.createSingleNote(
            id = null,
            title = "super cool",
            body = "test"
        )
        noteDaoService.insertNote(newNote)

        val newTitle = UUID.randomUUID().toString()
        val newBody = UUID.randomUUID().toString()

        noteDaoService.updateNote(
            primaryKey = newNote.id,
            title = newTitle,
            body = newBody,
            timestamp = null
        )

        val foundNote = noteDaoService.getAllNotes().filter { it.id == newNote.id }[0]
        assertTrue { foundNote.title == newTitle }
        assertTrue { foundNote.body == newBody }
        assertFalse { foundNote.updatedAt == newNote.updatedAt }
        assertTrue { foundNote.createdAt == newNote.createdAt }
    }

    @Test
    fun searchNotes_orderByDateASC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByDateASC(
            query = "",
            page = 1,
            pageSize = 100
        )

        var previousUpdatedAt = noteList[0].updatedAt
        noteList.forEach {
            assertTrue { it.updatedAt >= previousUpdatedAt }
            previousUpdatedAt = it.updatedAt
        }
    }

    @Test
    fun searchNotes_orderByDateDESC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByDateASC(
            query = "",
            page = 1,
            pageSize = 100
        )

        var previousUpdatedAt = noteList[0].updatedAt
        noteList.forEach {
            assertTrue { it.updatedAt <= previousUpdatedAt }
            previousUpdatedAt = it.updatedAt
        }
    }

    @Test
    fun searchNotes_orderByTitleASC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByDateASC(
            query = "",
            page = 1,
            pageSize = 100
        )

        var previousTitle = noteList[0].title
        noteList.forEach { note ->
            assertTrue {
                listOf(previousTitle, note.title)
                    .asSequence()
                    .zipWithNext { a, b -> a <= b }
                    .all { it }
            }
            previousTitle = note.title
        }
    }

    @Test
    fun searchNotes_orderByTitleDESC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByDateASC(
            query = "",
            page = 1,
            pageSize = 100
        )

        var previousTitle = noteList[0].title
        noteList.forEach { note ->
            assertTrue {
                listOf(previousTitle, note.title)
                    .asSequence()
                    .zipWithNext { a, b -> a >= b }
                    .all { it }
            }
            previousTitle = note.title
        }
    }
}