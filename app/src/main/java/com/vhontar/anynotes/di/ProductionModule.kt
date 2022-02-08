package com.vhontar.anynotes.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.vhontar.anynotes.datasource.cache.database.DATABASE_NAME
import com.vhontar.anynotes.datasource.cache.database.NoteDatabase
import com.vhontar.anynotes.datasource.preferences.PreferenceKeys
import com.vhontar.anynotes.presentation.BaseApplication
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@Module
object ProductionModule {
    @Singleton
    @Provides
    fun provideNoteDb(app: BaseApplication): NoteDatabase {
        return Room
            .databaseBuilder(app, NoteDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(
        application: BaseApplication
    ): SharedPreferences {
        return application
            .getSharedPreferences(
                PreferenceKeys.NOTE_PREFERENCES,
                Context.MODE_PRIVATE
            )
    }
}