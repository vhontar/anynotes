package com.vhontar.anynotes.framework.datasource.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vhontar.anynotes.framework.datasource.cache.model.NoteCacheEntity

const val DATABASE_NAME = "note_db"

@Database(
    entities = [
        NoteCacheEntity::class
    ],
    version = 1
)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}