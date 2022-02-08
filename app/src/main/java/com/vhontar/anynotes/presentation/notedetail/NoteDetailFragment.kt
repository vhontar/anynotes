package com.vhontar.anynotes.presentation.notedetail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.vhontar.anynotes.R
import com.vhontar.anynotes.presentation.common.BaseNoteFragment
import kotlinx.coroutines.FlowPreview

const val NOTE_DETAIL_STATE_BUNDLE_KEY = "com.codingwithmitch.cleannotes.notes.framework.presentation.notedetail.state"

@FlowPreview
class NoteDetailFragment constructor(
    factory: ViewModelProvider.Factory
) : BaseNoteFragment(R.layout.fragment_note_detail) {
    // private val viewModel: NoteDetailViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun inject() {
        TODO("prepare dagger")
    }


}














