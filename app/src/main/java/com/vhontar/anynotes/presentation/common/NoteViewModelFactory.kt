package com.vhontar.anynotes.presentation.common

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.business.usecases.notedetail.NoteDetailUseCases
import com.vhontar.anynotes.business.usecases.notelist.NoteListUseCases
import com.vhontar.anynotes.presentation.notedetail.NoteDetailViewModel
import com.vhontar.anynotes.presentation.notelist.NoteListViewModel
import com.vhontar.anynotes.presentation.splash.SplashViewModel
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

@FlowPreview
@Singleton
class NoteViewModelFactory @Inject constructor(
    private val noteListUseCases: NoteListUseCases,
    private val noteDetailUseCases: NoteDetailUseCases,
    private val noteFactory: NoteFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when(modelClass){
            NoteListViewModel::class.java -> {
                NoteListViewModel(
                    noteListUseCases = noteListUseCases,
                    noteFactory = noteFactory,
                    editor = editor,
                    sharedPreferences = sharedPreferences
                ) as T
            }
            NoteDetailViewModel::class.java -> {
                NoteDetailViewModel(
                    noteDetailUseCases = noteDetailUseCases
                ) as T
            }
            SplashViewModel::class.java -> {
                SplashViewModel() as T
            }

            else -> {
                throw IllegalArgumentException("unknown model class $modelClass")
            }
        }
    }
}
