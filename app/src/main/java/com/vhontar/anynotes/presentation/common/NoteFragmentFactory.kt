package com.vhontar.anynotes.presentation.common

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.vhontar.anynotes.presentation.notedetail.NoteDetailFragment
import com.vhontar.anynotes.presentation.notelist.NoteListFragment
import com.vhontar.anynotes.presentation.splash.SplashFragment
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
class NoteFragmentFactory @Inject constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): FragmentFactory(){
    override fun instantiate(classLoader: ClassLoader, className: String) =
        when(className){
            NoteListFragment::class.java.name -> {
                val fragment = NoteListFragment(viewModelFactory)
                fragment
            }
            NoteDetailFragment::class.java.name -> {
                val fragment = NoteDetailFragment(viewModelFactory)
                fragment
            }
            SplashFragment::class.java.name -> {
                val fragment = SplashFragment(viewModelFactory)
                fragment
            }
            else -> {
                super.instantiate(classLoader, className)
            }
        }
}