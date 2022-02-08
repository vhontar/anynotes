package com.vhontar.anynotes.di

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.vhontar.anynotes.presentation.common.NoteFragmentFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@Module
object NoteFragmentFactoryModule {
    @Singleton
    @Provides
    fun provideNoteFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory
    ): FragmentFactory {
        return NoteFragmentFactory(viewModelFactory)
    }
}
