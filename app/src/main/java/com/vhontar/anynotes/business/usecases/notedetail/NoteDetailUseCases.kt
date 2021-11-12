package com.vhontar.anynotes.business.usecases.notedetail

import com.vhontar.anynotes.business.usecases.common.DeleteNoteUseCase
import com.vhontar.anynotes.framework.presentation.notedetail.state.NoteDetailViewState

class NoteDetailUseCases(
    val updateNoteUseCase: UpdateNoteUseCase,
    val deleteNoteUseCase: DeleteNoteUseCase<NoteDetailViewState>
)