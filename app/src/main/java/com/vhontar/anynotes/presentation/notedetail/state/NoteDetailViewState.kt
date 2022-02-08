package com.vhontar.anynotes.presentation.notedetail.state

import android.os.Parcelable
import com.vhontar.anynotes.business.domain.model.Note
import com.vhontar.anynotes.business.domain.state.ViewState
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoteDetailViewState(
    var note: Note? = null,
    var isUpdatePending: Boolean? = null
) : Parcelable, ViewState