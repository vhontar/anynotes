package com.vhontar.anynotes.presentation.notelist

import android.content.SharedPreferences
import com.vhontar.anynotes.business.domain.model.NoteFactory
import com.vhontar.anynotes.business.domain.state.StateEvent
import com.vhontar.anynotes.business.usecases.notelist.NoteListUseCases
import com.vhontar.anynotes.presentation.common.BaseViewModel
import com.vhontar.anynotes.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
class NoteListViewModel @Inject constructor(
    private val noteListUseCases: NoteListUseCases,
    private val noteFactory: NoteFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences
): BaseViewModel<NoteListViewState>(){

    override fun handleNewData(data: NoteListViewState) {

    }

    override fun setStateEvent(stateEvent: StateEvent) {
    }

    override fun initNewViewState(): NoteListViewState {
        return NoteListViewState()
    }

}