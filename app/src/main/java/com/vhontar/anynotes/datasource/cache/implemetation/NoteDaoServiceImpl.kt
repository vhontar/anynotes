package com.vhontar.anynotes.datasource.cache.implemetation

import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.model.toDatabaseEntities
import com.vhontar.anynotes.business.domain.model.toDatabaseEntity
import com.vhontar.anynotes.business.domain.util.DateUtil
import com.vhontar.anynotes.datasource.cache.abstraction.NoteDaoService
import com.vhontar.anynotes.datasource.cache.database.NoteDao
import com.vhontar.anynotes.datasource.cache.database.returnOrderedQuery
import com.vhontar.anynotes.datasource.cache.model.toDomainModel
import com.vhontar.anynotes.datasource.cache.model.toDomainModels
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDaoServiceImpl @Inject constructor(
    private val noteDao: NoteDao
): NoteDaoService {
    override suspend fun insertNote(note: Note): Long = noteDao.insertNote(note.toDatabaseEntity())
    override suspend fun insertNotes(notes: List<Note>): LongArray = noteDao.insertNotes(notes.toDatabaseEntities())
    override suspend fun searchNoteById(id: String): Note? = noteDao.searchNoteById(id)?.toDomainModel()
    override suspend fun updateNote(primaryKey: String, title: String, body: String?, timestamp: String?): Int {
        return if (timestamp == null) {
            noteDao.updateNote(primaryKey, title, body, DateUtil.getCurrentTimestamp())
        } else {
            noteDao.updateNote(primaryKey, title, body, timestamp)
        }
    }
    override suspend fun deleteNote(primaryKey: String): Int = noteDao.deleteNote(primaryKey)
    override suspend fun deleteNotes(notes: List<Note>): Int = noteDao.deleteNotes(notes.map { it.id })
    override suspend fun searchNotes(): List<Note> = noteDao.getAllNotes().toDomainModels()
    override suspend fun getAllNotes(): List<Note> = noteDao.getAllNotes().toDomainModels()
    override suspend fun searchNotesOrderByDateDESC(query: String, page: Int, pageSize: Int): List<Note> =
        noteDao.searchNotesOrderByDateDESC(query, page, pageSize).toDomainModels()
    override suspend fun searchNotesOrderByDateASC(query: String, page: Int, pageSize: Int): List<Note> =
        noteDao.searchNotesOrderByDateASC(query, page, pageSize).toDomainModels()
    override suspend fun searchNotesOrderByTitleDESC(query: String, page: Int, pageSize: Int): List<Note> =
        noteDao.searchNotesOrderByTitleDESC(query, page, pageSize).toDomainModels()
    override suspend fun searchNotesOrderByTitleASC(query: String, page: Int, pageSize: Int): List<Note> =
        noteDao.searchNotesOrderByTitleASC(query, page, pageSize).toDomainModels()
    override suspend fun getNumNotes(): Int = noteDao.getNumNotes()
    override suspend fun returnOrderedQuery(query: String, filterAndOrder: String, page: Int): List<Note> =
        noteDao.returnOrderedQuery(query, filterAndOrder, page).toDomainModels()
}