package com.vhontar.anynotes.di

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vhontar.anynotes.business.data.cache.abstraction.NoteCacheDataSource
import com.vhontar.anynotes.business.data.cache.implementation.NoteCacheDataSourceImpl
import com.vhontar.anynotes.business.data.network.abstraction.NoteNetworkDataSource
import com.vhontar.anynotes.business.data.network.implementation.NoteNetworkDataSourceImpl
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.business.usecases.common.DeleteNoteUseCase
import com.vhontar.anynotes.business.usecases.notedetail.NoteDetailUseCases
import com.vhontar.anynotes.business.usecases.notedetail.UpdateNoteUseCase
import com.vhontar.anynotes.business.usecases.notelist.*
import com.vhontar.anynotes.business.usecases.splash.SyncDeletedNotesUseCase
import com.vhontar.anynotes.business.usecases.splash.SyncNotesUseCase
import com.vhontar.anynotes.framework.datasource.cache.abstraction.NoteDaoService
import com.vhontar.anynotes.framework.datasource.cache.database.NoteDao
import com.vhontar.anynotes.framework.datasource.cache.database.NoteDatabase
import com.vhontar.anynotes.framework.datasource.cache.implemetation.NoteDaoServiceImpl
import com.vhontar.anynotes.framework.datasource.network.abstraction.NoteFirestoreService
import com.vhontar.anynotes.framework.datasource.network.implementation.NoteFirestoreServiceImpl
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@Module
object AppModule {
    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPrefsEditor(
        sharedPreferences: SharedPreferences
    ): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDAO(noteDatabase: NoteDatabase): NoteDao {
        return noteDatabase.noteDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDaoService(
        noteDao: NoteDao
    ): NoteDaoService {
        return NoteDaoServiceImpl(noteDao)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteCacheDataSource(
        noteDaoService: NoteDaoService
    ): NoteCacheDataSource {
        return NoteCacheDataSourceImpl(noteDaoService)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreService(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore
    ): NoteFirestoreService {
        return NoteFirestoreServiceImpl(
            firebaseAuth,
            firebaseFirestore
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkDataSource(
        firestoreService: NoteFirestoreServiceImpl
    ): NoteNetworkDataSource {
        return NoteNetworkDataSourceImpl(
            firestoreService
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncNotesUseCase(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource
    ): SyncNotesUseCase {
        return SyncNotesUseCase(
            noteCacheDataSource,
            noteNetworkDataSource
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncDeletedNotesUseCase(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource
    ): SyncDeletedNotesUseCase {
        return SyncDeletedNotesUseCase(
            noteCacheDataSource,
            noteNetworkDataSource
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDetailUseCases(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource
    ): NoteDetailUseCases {
        return NoteDetailUseCases(
            UpdateNoteUseCase(noteCacheDataSource, noteNetworkDataSource),
            DeleteNoteUseCase(noteCacheDataSource, noteNetworkDataSource)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteListUseCases(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource,
        noteFactory: NoteFactory
    ): NoteListUseCases {
        return NoteListUseCases(
            InsertNoteUseCase(noteCacheDataSource, noteNetworkDataSource, noteFactory),
            DeleteNoteUseCase(noteCacheDataSource, noteNetworkDataSource),
            SearchNotesUseCase(noteCacheDataSource),
            GetNumNotesUseCase(noteCacheDataSource),
            RestoreDeletedNoteUseCase(noteCacheDataSource, noteNetworkDataSource),
            DeleteMultipleNotesUseCase(noteCacheDataSource, noteNetworkDataSource)
        )
    }

    @Singleton
    @Provides
    fun provideNoteFactory() = NoteFactory()

//    @JvmStatic
//    @Singleton
//    @Provides
//    fun provideNoteNetworkSyncManager(
//        syncNotesUseCase: SyncNotesUseCase,
//        deletedNotesUseCase: SyncDeletedNotesUseCase
//    ): NoteNetworkSyncManager {
//        return NoteNetworkSyncManager(
//            syncNotesUseCase,
//            deletedNotesUseCase
//        )
//    }
}