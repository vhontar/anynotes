package com.vhontar.anynotes.business.usecases.notelist

import com.vhontar.anynotes.business.usecases.common.DeleteNoteUseCase
import com.vhontar.anynotes.presentation.notelist.state.NoteListViewState

class NoteListUseCases(
    val insertNoteUseCase: InsertNoteUseCase,
    val deleteNoteUseCase: DeleteNoteUseCase<NoteListViewState>,
    val searchNotesUseCase: SearchNotesUseCase,
    val getNumNotesUseCase: GetNumNotesUseCase,
    val restoreDeletedNoteUseCase: RestoreDeletedNoteUseCase,
    val deleteMultipleNotesUseCase: DeleteMultipleNotesUseCase
)