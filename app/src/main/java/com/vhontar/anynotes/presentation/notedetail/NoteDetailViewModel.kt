package com.vhontar.anynotes.presentation.notedetail

import com.vhontar.anynotes.business.domain.state.StateEvent
import com.vhontar.anynotes.business.usecases.notedetail.NoteDetailUseCases
import com.vhontar.anynotes.presentation.common.BaseViewModel
import com.vhontar.anynotes.presentation.notedetail.state.NoteDetailViewState
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
class NoteDetailViewModel @Inject constructor(
    private val noteDetailUseCases: NoteDetailUseCases
): BaseViewModel<NoteDetailViewState>(){

    override fun handleNewData(data: NoteDetailViewState) {

    }

    override fun setStateEvent(stateEvent: StateEvent) {

    }

    override fun initNewViewState(): NoteDetailViewState {
        return NoteDetailViewState()
    }

}
