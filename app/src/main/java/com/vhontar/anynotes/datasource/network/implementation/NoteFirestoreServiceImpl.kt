package com.vhontar.anynotes.datasource.network.implementation

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.model.toNetworkEntity
import com.vhontar.anynotes.datasource.network.abstraction.NoteFirestoreService
import com.vhontar.anynotes.datasource.network.model.NoteNetworkEntity
import com.vhontar.anynotes.datasource.network.model.toDomainModel
import com.vhontar.anynotes.datasource.network.model.toDomainModels
import com.vhontar.anynotes.util.sendCrashlytics
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteFirestoreServiceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : NoteFirestoreService {
    override suspend fun insertOrUpdateNote(note: Note) {
        val entity = note.toNetworkEntity()
        entity.updatedAt = Timestamp.now()

        getFirestoreNotesCollection()
            .document(entity.id)
            .set(entity)
            .addOnFailureListener {
                sendCrashlytics(it.message)
            }
            .await()
    }

    override suspend fun insertOrUpdateNotes(notes: List<Note>) {
        // can happen only in instrumental tests
        if (notes.size > 500) {
            throw Exception("Cannot insert more than 500 notes at a time into firestore.")
        }

        val collectionRef = getFirestoreNotesCollection()

        firestore.runBatch { batch ->
            notes.forEach {
                val entity = it.toNetworkEntity()
                entity.updatedAt = Timestamp.now()
                val documentRef = collectionRef.document(entity.id)
                batch.set(documentRef, entity)
            }
        }
            .addOnFailureListener {
                sendCrashlytics(it.message)
            }
            .await()
    }

    override suspend fun deleteNote(primaryKey: String) {
        getFirestoreNotesCollection()
            .document(primaryKey)
            .delete()
            .addOnFailureListener {
                sendCrashlytics(it.message)
            }
            .await()
    }

    override suspend fun insertDeletedNote(note: Note) {
        getFirestoreDeletedNotesCollection()
            .document(note.id)
            .set(note.toNetworkEntity())
            .addOnFailureListener {
                sendCrashlytics(it.message)
            }
            .await()
    }

    override suspend fun insertDeletedNotes(notes: List<Note>) {
        // can happen only in instrumental tests
        if (notes.size > 500) {
            throw Exception("Cannot insert more than 500 notes at a time into firestore.")
        }

        val collectionRef = getFirestoreDeletedNotesCollection()

        firestore.runBatch { batch ->
            notes.forEach {
                val documentRef = collectionRef.document(it.id)
                batch.set(documentRef, it.toNetworkEntity())
            }
        }
            .addOnFailureListener {
                sendCrashlytics(it.message)
            }
            .await()
    }

    override suspend fun deleteDeletedNote(note: Note) {
        getFirestoreDeletedNotesCollection()
            .document(note.id)
            .delete()
            .addOnFailureListener {
                sendCrashlytics(it.message)
            }
            .await()
    }

    override suspend fun getDeletedNotes(): List<Note> {
        return getFirestoreNotesCollection()
            .get()
            .addOnFailureListener {
                sendCrashlytics(it.message)
            }
            .await()
            .toObjects(NoteNetworkEntity::class.java)
            .toDomainModels()
    }

    override suspend fun deleteAllNotes() {
        firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .delete()
            .addOnFailureListener {
                sendCrashlytics(it.message)
            }
            .await()

        firestore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .delete()
            .addOnFailureListener {
                sendCrashlytics(it.message)
            }
            .await()
    }

    override suspend fun searchNote(note: Note): Note? {
        return getFirestoreNotesCollection()
            .document(note.id)
            .get()
            .addOnFailureListener {
                sendCrashlytics(it.message)
            }
            .await()
            .toObject(NoteNetworkEntity::class.java)
            ?.toDomainModel()
    }

    override suspend fun getAllNotes(): List<Note> {
        return getFirestoreNotesCollection()
            .get()
            .addOnFailureListener {
                sendCrashlytics(it.message)
            }
            .await()
            .toObjects(NoteNetworkEntity::class.java)
            .toDomainModels()
    }

    private fun getFirestoreNotesCollection(): CollectionReference {
        return firestore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
    }

    private fun getFirestoreDeletedNotesCollection(): CollectionReference {
        return firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
    }

    companion object {
        const val NOTES_COLLECTION = "notes"
        const val USERS_COLLECTION = "users"
        const val DELETES_COLLECTION = "deletes"
        const val USER_ID = "KGrn7yWhvhYlvpHTjMfIVzTKbfE2" // hardcoded for single user
        const val EMAIL = "vladislav.hontar@gmail.com"
    }
}