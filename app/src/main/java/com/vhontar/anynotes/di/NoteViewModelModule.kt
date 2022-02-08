package com.vhontar.anynotes.di

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.business.usecases.notedetail.NoteDetailUseCases
import com.vhontar.anynotes.business.usecases.notelist.NoteListUseCases
import com.vhontar.anynotes.presentation.common.NoteViewModelFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@Module
object NoteViewModelModule {
    @Singleton
    @Provides
    fun provideNoteViewModelFactory(
        noteListUseCases: NoteListUseCases,
        noteDetailUseCases: NoteDetailUseCases,
        noteFactory: NoteFactory,
        editor: SharedPreferences.Editor,
        sharedPreferences: SharedPreferences
    ): ViewModelProvider.Factory {
        return NoteViewModelFactory(
            noteListUseCases = noteListUseCases,
            noteDetailUseCases = noteDetailUseCases,
            noteFactory = noteFactory,
            editor = editor,
            sharedPreferences = sharedPreferences
        )
    }
}