package cho.chonotes.framework.presentation.notedetail.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cho.chonotes.framework.presentation.notedetail.state.CollapsingToolbarState.*
import cho.chonotes.framework.presentation.notedetail.state.NoteInteractionState.*

class NoteInteractionManager{

    private val _noteTitleState: MutableLiveData<NoteInteractionState>
            = MutableLiveData(DefaultState())

    private val _noteBodyState: MutableLiveData<NoteInteractionState>
            = MutableLiveData(DefaultState())

    private val _collapsingToolbarState: MutableLiveData<CollapsingToolbarState>
            = MutableLiveData(Expanded())

    val noteTitleState: LiveData<NoteInteractionState>
            get() = _noteTitleState

    val noteBodyState: LiveData<NoteInteractionState>
        get() = _noteBodyState

    val collapsingToolbarState: LiveData<CollapsingToolbarState>
        get() = _collapsingToolbarState

    fun setCollapsingToolbarState(state: CollapsingToolbarState){
        if(state.toString() != _collapsingToolbarState.value.toString()){
            _collapsingToolbarState.value = state
        }
    }

    fun setNewNoteTitleState(state: NoteInteractionState){
        if(noteTitleState.toString() != state.toString()){
            _noteTitleState.value = state
            when(state){

                is EditState -> {
                    _noteBodyState.value = DefaultState()
                }
            }
        }
    }

    fun setNewNoteBodyState(state: NoteInteractionState){
        if(noteBodyState.toString() != state.toString()){
            _noteBodyState.value = state
            when(state){

                is EditState -> {
                    _noteTitleState.value = DefaultState()
                }
            }
        }
    }

    fun isEditingTitle() = noteTitleState.value.toString() == EditState().toString()

    fun isEditingBody() = noteBodyState.value.toString() == EditState().toString()

    fun exitEditState(){
        _noteTitleState.value = DefaultState()
        _noteBodyState.value = DefaultState()
    }

    fun checkEditState() = noteTitleState.value.toString() == EditState().toString()
            || noteBodyState.value.toString() == EditState().toString()

}

















