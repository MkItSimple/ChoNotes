package cho.chonotes.framework.presentation.notedetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cho.chonotes.R
import cho.chonotes.R.drawable
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.interactors.common.DeleteNote.Companion.DELETE_ARE_YOU_SURE
import cho.chonotes.business.interactors.common.DeleteNote.Companion.DELETE_NOTE_SUCCESS
import cho.chonotes.business.interactors.notedetail.UpdateNote.Companion.UPDATE_NOTE_FAILED_PK
import cho.chonotes.business.interactors.notedetail.UpdateNote.Companion.UPDATE_NOTE_SUCCESS
import cho.chonotes.framework.presentation.common.*
import cho.chonotes.framework.presentation.notedetail.state.CollapsingToolbarState.Collapsed
import cho.chonotes.framework.presentation.notedetail.state.CollapsingToolbarState.Expanded
import cho.chonotes.framework.presentation.notedetail.state.NoteDetailStateEvent.CreateStateMessageEvent
import cho.chonotes.framework.presentation.notedetail.state.NoteDetailStateEvent.UpdateNoteEvent
import cho.chonotes.framework.presentation.notedetail.state.NoteDetailViewState
import cho.chonotes.framework.presentation.notedetail.state.NoteInteractionState.DefaultState
import cho.chonotes.framework.presentation.notedetail.state.NoteInteractionState.EditState
import cho.chonotes.framework.presentation.notelist.NOTE_PENDING_DELETE_BUNDLE_KEY
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_note_detail.*
import kotlinx.android.synthetic.main.layout_note_detail_toolbar.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

const val NOTE_DETAIL_STATE_BUNDLE_KEY = "cho.chonotes.notes.framework.presentation.notedetail.state"

@SuppressLint("UseCompatLoadingForDrawables")
@FlowPreview
@ExperimentalCoroutinesApi
class NoteDetailFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): BaseNoteFragment(R.layout.fragment_note_detail) {

    val viewModel: NoteDetailViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupChannel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupOnBackPressDispatcher()
        subscribeObservers()

        note_title.setOnClickListener {
            onClickNoteTitle()
        }

        note_body.setOnClickListener {
            onClickNoteBody()
        }

        getSelectedNoteFromPreviousFragment()
        restoreInstanceState()
    }

    private fun onErrorRetrievingNoteFromPreviousFragment(){
        viewModel.setStateEvent(
            CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = NOTE_DETAIL_ERROR_RETRIEVEING_SELECTED_NOTE,
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Error()
                    )
                )
            )
        )
    }

    private fun onClickNoteTitle(){
        if(!viewModel.isEditingTitle()){
            updateBodyInViewModel()
            updateNote()
            viewModel.setNoteInteractionTitleState(EditState())
        }
    }

    private fun onClickNoteBody(){
        if(!viewModel.isEditingBody()){
            updateTitleInViewModel()
            updateNote()
            viewModel.setNoteInteractionBodyState(EditState())

            setBottomViewHeight(700)
        }
    }

    private fun setBottomViewHeight(height: Int) {
        val params = bottom_view.layoutParams as LinearLayout.LayoutParams
        params.height = height
        bottom_view.layoutParams = params
    }

    private fun onBackPressed() {
        view?.hideKeyboard()
        if(viewModel.checkEditState()){
            updateBodyInViewModel()
            updateTitleInViewModel()
            updateNote()
            viewModel.exitEditState()
            displayDefaultToolbar()
            setBottomViewHeight(50)
        }
        else{
            findNavController().popBackStack()
        }
    }

    override fun onPause() {
        super.onPause()
        updateTitleInViewModel()
        updateBodyInViewModel()
        updateNote()
    }

    private fun subscribeObservers(){

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->

            if(viewState != null){

                viewState.note?.let { note ->
                    setNoteTitle(note.title)
                    setNoteBody(note.body)
                }
            }
        })

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, Observer {
            uiController.displayProgressBar(it)
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->

            stateMessage?.response?.let { response ->

                when(response.message){

                    UPDATE_NOTE_SUCCESS -> {
                        viewModel.setIsUpdatePending(false)
                        viewModel.clearStateMessage()
                    }

                    DELETE_NOTE_SUCCESS -> {
                        viewModel.clearStateMessage()
                        onDeleteSuccess()
                    }

                    else -> {
                        uiController.onResponseReceived(
                            response = stateMessage.response,
                            stateMessageCallback = object: StateMessageCallback {
                                override fun removeMessageFromStack() {
                                    viewModel.clearStateMessage()
                                }
                            }
                        )
                        when(response.message){

                            UPDATE_NOTE_FAILED_PK -> {
                                findNavController().popBackStack()
                            }

                            NOTE_DETAIL_ERROR_RETRIEVEING_SELECTED_NOTE -> {
                                findNavController().popBackStack()
                            }

                            else -> {}
                        }
                    }
                }
            }

        })

        viewModel.collapsingToolbarState.observe(viewLifecycleOwner, Observer { state ->

            when(state){

                is Expanded -> {
                    transitionToExpandedMode()
                }

                is Collapsed -> {
                    transitionToCollapsedMode()
                }
            }
        })

        viewModel.noteTitleInteractionState.observe(viewLifecycleOwner, Observer { state ->

            when(state){

                is EditState -> {
                    note_title.enableContentInteraction()
                    view?.showKeyboard()
                    displayEditStateToolbar()
                    viewModel.setIsUpdatePending(true)
                }

                is DefaultState -> {
                    note_title.disableContentInteraction()
                }
            }
        })

        viewModel.noteBodyInteractionState.observe(viewLifecycleOwner, Observer { state ->

            when(state){

                is EditState -> {
                    note_body.enableContentInteraction()
                    view?.showKeyboard()
                    displayEditStateToolbar()
                    viewModel.setIsUpdatePending(true)
                }

                is DefaultState -> {
                    note_body.disableContentInteraction()
                }
            }
        })
    }


    private fun displayDefaultToolbar(){
        activity?.let { a ->
            toolbar_primary_icon.setImageDrawable(
                resources.getDrawable(
                    drawable.ic_arrow_back_grey_24dp,
                    a.application.theme
                )
            )
            toolbar_secondary_icon.setImageDrawable(
                resources.getDrawable(
                    drawable.ic_baseline_delete_24,
                    a.application.theme
                )
            )
        }
    }

    private fun displayEditStateToolbar(){
        activity?.let { a ->
            toolbar_primary_icon.setImageDrawable(
                resources.getDrawable(
                    drawable.ic_close_grey_24dp,
                    a.application.theme
                )
            )
            toolbar_secondary_icon.setImageDrawable(
                resources.getDrawable(
                    drawable.ic_done_grey_24dp,
                    a.application.theme
                )
            )
        }
    }

    private fun setNoteTitle(title: String) {
        note_title.setText(title)
    }

    private fun getNoteTitle(): String{
        return note_title.text.toString()
    }

    private fun getNoteBody(): String{
        return note_body.text.toString()
    }

    private fun setNoteBody(body: String?){
        note_body.setText(body)
    }

    private fun getSelectedNoteFromPreviousFragment(){
        arguments?.let { args ->
            (args.getParcelable(NOTE_DETAIL_SELECTED_NOTE_BUNDLE_KEY) as Note?)?.let { selectedNote ->
                viewModel.setNote(selectedNote)
            }?: onErrorRetrievingNoteFromPreviousFragment()
        }

    }

    private fun restoreInstanceState(){
        arguments?.let { args ->
            (args.getParcelable(NOTE_DETAIL_STATE_BUNDLE_KEY) as NoteDetailViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)

                if(viewModel.isToolbarCollapsed()){
                    app_bar.setExpanded(false)
                    transitionToCollapsedMode()
                }
                else{
                    app_bar.setExpanded(true)
                    transitionToExpandedMode()
                }
            }
        }
    }

    private fun updateTitleInViewModel(){
        if(viewModel.isEditingTitle()){
            viewModel.updateNoteTitle(getNoteTitle())
        }
    }

    private fun updateBodyInViewModel(){
        if(viewModel.isEditingBody()){
            viewModel.updateNoteBody(getNoteBody())
        }
    }

    private fun setupUI(){
        note_title.disableContentInteraction()
        note_body.disableContentInteraction()
        displayDefaultToolbar()
        transitionToExpandedMode()

        app_bar.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener{ _, offset ->

                if(offset < COLLAPSING_TOOLBAR_VISIBILITY_THRESHOLD){
                    updateTitleInViewModel()
                    if(viewModel.isEditingTitle()){
                        viewModel.exitEditState()
                        displayDefaultToolbar()
                        updateNote()
                    }
                    viewModel.setCollapsingToolbarState(Collapsed())
                }
                else{
                    viewModel.setCollapsingToolbarState(Expanded())
                }
            })

        toolbar_primary_icon.setOnClickListener {
            if(viewModel.checkEditState()){
                view?.hideKeyboard()
                viewModel.triggerNoteObservers()
                viewModel.exitEditState()
                displayDefaultToolbar()
                setBottomViewHeight(50)
            }
            else{
                onBackPressed()
            }
        }

        toolbar_secondary_icon.setOnClickListener {
            if(viewModel.checkEditState()){
                view?.hideKeyboard()
                updateTitleInViewModel()
                updateBodyInViewModel()
                updateNote()
                viewModel.exitEditState()
                displayDefaultToolbar()
                setBottomViewHeight(50)
            }
            else{
                deleteNote()
            }
        }
    }

    private fun deleteNote(){
        viewModel.setStateEvent(
            CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = DELETE_ARE_YOU_SURE,
                        uiComponentType = UIComponentType.AreYouSureDialog(
                            object: AreYouSureCallback{
                                override fun proceed() {
                                    viewModel.getNote()?.let{ note ->
                                        initiateDeleteTransaction(note)
                                    }
                                }

                                override fun cancel() {}
                            }
                        ),
                        messageType = MessageType.Info()
                    )
                )
            )
        )
    }

    private fun initiateDeleteTransaction(note: Note){
        viewModel.beginPendingDelete(note)
    }

    private fun onDeleteSuccess(){
        val bundle = bundleOf(NOTE_PENDING_DELETE_BUNDLE_KEY to viewModel.getNote())
        viewModel.setNote(null)
        viewModel.setIsUpdatePending(false)
        findNavController().navigate(
            R.id.action_note_detail_fragment_to_noteListFragment,
            bundle
        )
    }

    private fun setupOnBackPressDispatcher() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }


    private fun updateNote() {
        if(viewModel.getIsUpdatePending()){
            viewModel.setStateEvent(
                UpdateNoteEvent()
            )
        }
    }

    private fun transitionToCollapsedMode() {
        note_title.fadeOut()
        displayToolbarTitle(tool_bar_title, getNoteTitle(), true)
    }

    private fun transitionToExpandedMode() {
        note_title.fadeIn()
        displayToolbarTitle(tool_bar_title, null, true)
    }

    override fun inject() {
        getAppComponent().inject(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.getCurrentViewStateOrNew()
        outState.putParcelable(NOTE_DETAIL_STATE_BUNDLE_KEY, viewState)
        super.onSaveInstanceState(outState)
    }


}














