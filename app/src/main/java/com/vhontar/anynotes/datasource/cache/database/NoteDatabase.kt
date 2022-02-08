package com.vhontar.anynotes.datasource.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vhontar.anynotes.datasource.cache.model.NoteCacheEntity

const val DATABASE_NAME = "note_db"

@Database(
    entities = [
        NoteCacheEntity::class
    ],
    version = 1
    // exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}