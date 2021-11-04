package com.vhontar.anynotes.framework.datasource.cache.abstraction

import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.framework.datasource.database.NoteDao

interface NoteDaoService {
    suspend fun insertNote(note: Note): Long
    suspend fun insertNotes(notes: List<Note>): LongArray
    suspend fun searchNoteById(id: String): Note?
    suspend fun updateNote(
        primaryKey: String,
        title: String,
        body: String?
    ): Long
    suspend fun deleteNote(primaryKey: String): Int
    suspend fun deleteNotes(notes: List<Note>): Int
    suspend fun searchNotes(): List<Note>
    suspend fun getAllNotes(): List<Note>
    suspend fun searchNotesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = NoteDao.NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>
    suspend fun searchNotesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = NoteDao.NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>
    suspend fun searchNotesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int = NoteDao.NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>
    suspend fun searchNotesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int = NoteDao.NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>
    suspend fun getNumNotes(): Int
    suspend fun returnOrderedQuery(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Note>
}