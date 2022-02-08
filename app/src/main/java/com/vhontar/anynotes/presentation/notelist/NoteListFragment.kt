package com.vhontar.anynotes.presentation.notelist

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.vhontar.anynotes.R
import com.vhontar.anynotes.presentation.common.BaseNoteFragment
import kotlinx.coroutines.FlowPreview

const val NOTE_LIST_STATE_BUNDLE_KEY = "com.codingwithmitch.cleannotes.notes.framework.presentation.notelist.state"

@FlowPreview
class NoteListFragment constructor(
    factory: ViewModelProvider.Factory
) : BaseNoteFragment(R.layout.fragment_note_list) {
    // private val viewModel: NoteListViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun inject() {
        TODO("prepare dagger")
    }

}










































