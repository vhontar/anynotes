package com.vhontar.anynotes.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.vhontar.anynotes.framework.datasource.cache.database.DATABASE_NAME
import com.vhontar.anynotes.framework.datasource.cache.database.NoteDatabase
import com.vhontar.anynotes.framework.presentation.BaseApplication
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@Module
object ProductionModule {
    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDb(app: BaseApplication): NoteDatabase {
        return Room
            .databaseBuilder(app, NoteDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}