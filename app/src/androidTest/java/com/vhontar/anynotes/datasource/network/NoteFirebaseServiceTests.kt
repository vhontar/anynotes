package com.vhontar.anynotes.datasource.network

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.model.toNetworkEntities
import com.vhontar.anynotes.di.TestAppComponent
import com.vhontar.anynotes.BaseTest
import com.vhontar.anynotes.datasource.data.NoteDataFactory
import com.vhontar.anynotes.datasource.network.abstraction.NoteFirestoreService
import com.vhontar.anynotes.datasource.network.implementation.NoteFirestoreServiceImpl
import com.vhontar.anynotes.datasource.network.implementation.NoteFirestoreServiceImpl.Companion.NOTES_COLLECTION
import com.vhontar.anynotes.datasource.network.implementation.NoteFirestoreServiceImpl.Companion.USER_ID
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject
import kotlin.random.Random.Default.nextInt
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
    LEGEND:
    1. CBS = "Confirm by searching"
    Test cases:
    1. insert a single note, CBS
    2. update a random note, CBS
    3. insert a list of notes, CBS
    4. delete a single note, CBS
    5. insert a deleted note into "deletes" node, CBS
    6. insert a list of deleted notes into "deletes" node, CBS
    7. delete a 'deleted note' (note from "deletes" node). CBS
 */
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class NoteFirestoreServiceTests : BaseTest() {

    // system in test
    private lateinit var noteFirestoreService: NoteFirestoreService

    @Inject
    lateinit var firestore: FirebaseFirestore
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    @Inject
    lateinit var noteDataFactory: NoteDataFactory

    init {
        injectTest()
        signIn()
        insertTestData()
    }

    @Before
    fun before() {
        noteFirestoreService = NoteFirestoreServiceImpl(
            firebaseAuth = FirebaseAuth.getInstance(),
            firestore = firestore
        )
    }

    override fun injectTest() {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }

    private fun signIn() = runBlocking {
        firebaseAuth.signInWithEmailAndPassword(
            EMAIL,
            PASSWORD
        ).await()
    }

    private fun insertTestData() {
        noteDataFactory.produceListOfNotes().toNetworkEntities().forEach {
            firestore
                .collection(NOTES_COLLECTION)
                .document(USER_ID)
                .collection(NOTES_COLLECTION)
                .document(it.id)
                .set(it)
        }
    }

    @Test
    fun insertSingleNote_CBS() = runBlocking {
        val note = noteDataFactory.createSingleNote(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )

        noteFirestoreService.insertOrUpdateNote(note)

        val searchResult = noteFirestoreService.searchNote(note)

        assertEquals(note, searchResult)
    }

    @Test
    fun updateSingleNote_CBS() = runBlocking {

        val searchResults = noteFirestoreService.getAllNotes()

        // choose a random note from list to update
        val randomNote = searchResults[nextInt(0, searchResults.size - 1) + 1]
        val updatedTitle = UUID.randomUUID().toString()
        val updatedBody = UUID.randomUUID().toString()
        var updatedNote = noteDataFactory.createSingleNote(
            id = randomNote.id,
            title = updatedTitle,
            body = updatedBody
        )

        // make the update
        noteFirestoreService.insertOrUpdateNote(updatedNote)

        // query the note after update
        updatedNote = noteFirestoreService.searchNote(updatedNote)!!

        assertEquals(updatedTitle, updatedNote.title)
        assertEquals(updatedBody, updatedNote.body)
    }

    @Test
    fun insertNoteList_CBS() = runBlocking {
        val list = noteDataFactory.produceListOfNotes()

        noteFirestoreService.insertOrUpdateNotes(list)

        val searchResults = noteFirestoreService.getAllNotes()

        assertTrue { searchResults.containsAll(list) }
    }

    @Test
    fun deleteSingleNote_CBS() = runBlocking {
        val noteList = noteFirestoreService.getAllNotes()

        // choose one at random to delete
        val noteToDelete = noteList[nextInt(0, noteList.size - 1) + 1]

        noteFirestoreService.deleteNote(noteToDelete.id)

        // confirm it no longer exists in firestore
        val searchResults = noteFirestoreService.getAllNotes()

        assertFalse { searchResults.contains(noteToDelete) }
    }

    @Test
    fun insertIntoDeletesNode_CBS() = runBlocking {
        val noteList = noteFirestoreService.getAllNotes()

        // choose one at random to insert into "deletes" node
        val noteToDelete = noteList[nextInt(0, noteList.size - 1) + 1]

        noteFirestoreService.insertDeletedNote(noteToDelete)

        // confirm it is now in the "deletes" node
        val searchResults = noteFirestoreService.getDeletedNotes()

        assertTrue { searchResults.contains(noteToDelete) }
    }

    @Test
    fun insertListIntoDeletesNode_CBS() = runBlocking {
        val noteList = ArrayList(noteFirestoreService.getAllNotes())

        // choose some random notes to add to "deletes" node
        val notesToDelete: ArrayList<Note> = ArrayList()

        // 1st
        var noteToDelete = noteList[nextInt(0, noteList.size - 1) + 1]
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 2nd
        noteToDelete = noteList[nextInt(0, noteList.size - 1) + 1]
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 3rd
        noteToDelete = noteList[nextInt(0, noteList.size - 1) + 1]
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 4th
        noteToDelete = noteList[nextInt(0, noteList.size - 1) + 1]
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // insert into "deletes" node
        noteFirestoreService
            .insertDeletedNotes(notesToDelete)

        // confirm the notes are in "deletes" node
        val searchResults = noteFirestoreService.getDeletedNotes()

        assertTrue { searchResults.containsAll(notesToDelete) }
    }

    @Test
    fun deleteDeletedNote_CBS() = runBlocking {
        val note = noteDataFactory.createSingleNote(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )

        // insert into "deletes" node
        noteFirestoreService.insertDeletedNote(note)

        // confirm note is in "deletes" node
        var searchResults = noteFirestoreService.getDeletedNotes()

        assertTrue { searchResults.contains(note) }

        // delete from "deletes" node
        noteFirestoreService.deleteDeletedNote(note)

        // confirm note is deleted from "deletes" node
        searchResults = noteFirestoreService.getDeletedNotes()

        assertFalse { searchResults.contains(note) }
    }

    companion object {
        const val EMAIL = "vladislav.hontar@gmail.com"
        const val PASSWORD = "sYhdo5-jobwih-tesbox"
    }
}