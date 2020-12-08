package cho.chonotes.framework.presentation.notelist

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import cho.chonotes.R
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.business.interactors.common.DeleteNote.Companion.DELETE_NOTE_PENDING
import cho.chonotes.business.interactors.common.DeleteNote.Companion.DELETE_NOTE_SUCCESS
import cho.chonotes.business.interactors.notelist.DeleteMultipleNotes.Companion.DELETE_NOTES_ARE_YOU_SURE
import cho.chonotes.framework.datasource.cache.database.NOTE_FILTER_DATE_CREATED
import cho.chonotes.framework.datasource.cache.database.NOTE_FILTER_TITLE
import cho.chonotes.framework.datasource.cache.database.NOTE_ORDER_ASC
import cho.chonotes.framework.datasource.cache.database.NOTE_ORDER_DESC
import cho.chonotes.framework.presentation.auth.AuthActivity
import cho.chonotes.framework.presentation.common.BaseNoteFragment
import cho.chonotes.framework.presentation.common.TopSpacingItemDecoration
import cho.chonotes.framework.presentation.common.hideKeyboard
import cho.chonotes.framework.presentation.folderlist.FOLDER_LIST_SELECTED_NOTES_BUNDLE_KEY
import cho.chonotes.framework.presentation.folderlist.FolderListFragment
import cho.chonotes.framework.presentation.notedetail.NOTE_DETAIL_SELECTED_NOTE_BUNDLE_KEY
import cho.chonotes.framework.presentation.notelist.state.NoteListStateEvent.*
import cho.chonotes.framework.presentation.notelist.state.NoteListToolbarState.MultiSelectionState
import cho.chonotes.framework.presentation.notelist.state.NoteListToolbarState.SearchViewState
import cho.chonotes.framework.presentation.notelist.state.NoteListViewState
import cho.chonotes.util.AndroidTestUtils
import cho.chonotes.util.TodoCallback
import cho.chonotes.util.printLogD
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_note_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

const val NOTE_LIST_STATE_BUNDLE_KEY = "cho.chonotes.notes.framework.presentation.notelist.state"

@FlowPreview
@ExperimentalCoroutinesApi
class NoteListFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
): BaseNoteFragment(R.layout.fragment_note_list),
    NoteListAdapter.Interaction,
    ItemTouchHelperAdapter
{

    @Inject
    lateinit var androidTestUtils: AndroidTestUtils

    val viewModel: NoteListViewModel by viewModels {
        viewModelFactory
    }

    private var listAdapter: NoteListAdapter? = null
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupChannel()
        arguments?.let { args ->
            args.getParcelable<Note>(NOTE_PENDING_DELETE_BUNDLE_KEY)?.let { note ->
                viewModel.setNotePendingDelete(note)
                showUndoSnackbar_deleteNote()
                clearArgs()
            }
        }
    }

    private fun clearArgs(){
        arguments?.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        setupSwipeRefresh()
        setupFAB()
        subscribeObservers()

        getSelectedFolderFromPreviousFragment()
        restoreInstanceState(savedInstanceState)
    }

    private fun onErrorRetrievingFolderFromPreviousFragment(){
        viewModel.setStateEvent(
            CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = NOTE_LIST_ERROR_RETRIEVEING_SELECTED_FOLDER,
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Error()
                    )
                )
            )
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveNumNotesInCache()
        viewModel.clearList()
        viewModel.refreshSearchQuery()
    }

    override fun onPause() {
        super.onPause()
        saveLayoutManagerState()
    }

    private fun getSelectedFolderFromPreviousFragment(){
//        Log.d("folderSelected", "folderSelected")
        arguments?.let { args ->
            // From what fragment ?
            // Selected Folder ?
            // Selected Notes ?

            (args.getString(NOTE_LIST_PREVIOUS_FRAGMENT_BUNDLE_KEY))?.let { previousFragmentName ->
                (args.getParcelable(NOTE_LIST_SELECTED_FOLDER_BUNDLE_KEY) as Folder?)?.let { selectedFolder ->

                    if (previousFragmentName == "FolderListFragment") {
                        // This is for sorting notes by folder
                         Log.d("selectedFolderId", "selectedFolderId: ${selectedFolder.folder_id}")
                        viewModel.setSelectedFolderId(selectedFolder.folder_id)
                        startNewSearch()
                    } else {
                        (args.getParcelableArrayList<Note>(NOTE_LIST_SELECTED_NOTES_BUNDLE_KEY) as List<Note>?)?.let { selectedNotes ->

                            selectedFolder.folder_id.let {
                                viewModel.setStateEvent(
                                    MoveNotesEvent(
                                        selectedNotes = selectedNotes,
                                        folderId = selectedFolder.folder_id
                                    )
                                )
                            }

                        }
                    }

                }?: onErrorRetrievingFolderFromPreviousFragment()
            }

            clearArgs()

//            (args.getParcelable(NOTE_LIST_SELECTED_FOLDER_BUNDLE_KEY) as Folder?)?.let { selectedFolder ->
//
//                (args.getParcelableArrayList<Note>(NOTE_LIST_SELECTED_NOTES_BUNDLE_KEY) as List<Note>?)?.let { selectedNotes ->
//
//                    selectedFolder.folder_id?.let {
//                        viewModel.setStateEvent(
//                            UpdateMultipleNotesEvent(
//                                selectedNotes = selectedNotes,
//                                folderId = selectedFolder.folder_id
//                            )
//                        )
//                    }
//
//                }
//            }


//            (args.getParcelable(NOTE_LIST_SELECTED_FOLDER_BUNDLE_KEY) as Folder?)?.let { selectedFolder ->
//                // This is for sorting notes by folder
//                Log.d("selectedFolderId", "selectedFolderId: ${selectedFolder.folder_id}")
//                viewModel.setQuery(selectedFolder.folder_id)
//            }?: onErrorRetrievingFolderFromPreviousFragment()
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?){
        savedInstanceState?.let { inState ->
            (inState[NOTE_LIST_STATE_BUNDLE_KEY] as NoteListViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    // Why didn't I use the "SavedStateHandle" here?
    // It sucks and doesn't work for testing
    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.viewState.value

        //clear the list. Don't want to save a large list to bundle.
        viewState?.noteList =  ArrayList()

        outState.putParcelable(
            NOTE_LIST_STATE_BUNDLE_KEY,
            viewState
        )
        super.onSaveInstanceState(outState)
    }

    override fun restoreListPosition() {
        viewModel.getLayoutManagerState()?.let { lmState ->
            recycler_view?.layoutManager?.onRestoreInstanceState(lmState)
        }
    }

    private fun saveLayoutManagerState(){
        recycler_view.layoutManager?.onSaveInstanceState()?.let { lmState ->
            viewModel.setLayoutManagerState(lmState)
        }
    }

    private fun setupRecyclerView(){
        recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecorator = TopSpacingItemDecoration(20)
            addItemDecoration(topSpacingDecorator)
            itemTouchHelper = ItemTouchHelper(
                NoteItemTouchHelperCallback(
                    this@NoteListFragment,
                    viewModel.noteListInteractionManager
                )
            )
            listAdapter = NoteListAdapter(
                this@NoteListFragment,
                viewLifecycleOwner,
                viewModel.noteListInteractionManager.selectedNotes,
                dateUtil
            )
            itemTouchHelper?.attachToRecyclerView(this)
            addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == listAdapter?.itemCount?.minus(1)) {
                        viewModel.nextPage()
                    }
                }
            })
            adapter = listAdapter
        }
    }

    private fun enableMultiSelectToolbarState(){

        constraint_bottom.visibility = View.VISIBLE
        add_new_note_fab.visibility = View.GONE

        view?.let { v ->
            val view = View.inflate(
                v.context,
                R.layout.layout_multiselection_toolbar,
                null
            )
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            toolbar_content_container.addView(view)

//            val viewBottom = View.inflate(
//                v.context,
//                R.layout.layout_multiselection_toolbar_bottom,
//                null
//            )
//            viewBottom.layoutParams = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT
//            )
//            toolbar_content_container_bottom.addView(viewBottom)

            setupMultiSelectionToolbar(view)
        }
    }

    private fun setupMultiSelectionToolbar(
        parentView: View
    ){
        parentView
                .findViewById<ImageView>(R.id.action_exit_multiselect_state)
            .setOnClickListener {
                viewModel.setToolbarState(SearchViewState())
            }

//        parentView
//            .findViewById<ImageView>(R.id.action_delete_notes)
//            .setOnClickListener {
//                deleteNotes()
//            }

//        // added
//        parentViewBottom
//            .findViewById<ImageView>(R.id.move_imageview)
//            .setOnClickListener {
//                val selectedNotes = viewModel.noteListInteractionManager.selectedNotes
//                navigateToSelectFolderListFragment(selectedNotes)
//            }
        action_move_notes.setOnClickListener {
            val selectedNotes = viewModel.noteListInteractionManager.selectedNotes
            navigateToSelectFolderListFragment(selectedNotes)
        }


        action_delete_notes.setOnClickListener {
            deleteNotes()
        }
    }

    // added
    private fun navigateToSelectFolderListFragment(selectedNotes: LiveData<ArrayList<Note>>) {
        selectedNotes.value?.let {
            val bundle = bundleOf(FOLDER_LIST_SELECTED_NOTES_BUNDLE_KEY to it)
            findNavController().navigate(
                R.id.action_noteListFragment_to_selectFolderFragment,
                bundle
            )
        }
    }

    private fun enableSearchViewToolbarState(){

        constraint_bottom.visibility = View.GONE
        add_new_note_fab.visibility = View.VISIBLE

        view?.let { v ->
            val view = View.inflate(
                v.context,
                R.layout.layout_searchview_toolbar,
                null
            )
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            toolbar_content_container.addView(view)
            setupSearchView()
            setupFilterButton()
        }
    }

    private fun disableMultiSelectToolbarState(){
        view?.let {
            val view = toolbar_content_container
                .findViewById<Toolbar>(R.id.multiselect_toolbar)
            toolbar_content_container.removeView(view)
            viewModel.clearSelectedNotes()
        }
    }

    private fun disableSearchViewToolbarState(){
        view?.let {
            val view = toolbar_content_container
                .findViewById<Toolbar>(R.id.searchview_toolbar)
            toolbar_content_container.removeView(view)
        }
    }

    override fun isMultiSelectionModeEnabled()
            = viewModel.isMultiSelectionStateActive()

    override fun activateMultiSelectionMode()
            = viewModel.setToolbarState(MultiSelectionState())

    private fun subscribeObservers(){

        viewModel.toolbarState.observe(viewLifecycleOwner, Observer{ toolbarState ->

            when(toolbarState){

                is MultiSelectionState -> {
                    enableMultiSelectToolbarState()
                    disableSearchViewToolbarState()
                }

                is SearchViewState -> {
                    enableSearchViewToolbarState()
                    disableMultiSelectToolbarState()
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState ->

            if(viewState != null){
                viewState.noteList?.let { noteList ->
                    if(viewModel.isPaginationExhausted()
                        && !viewModel.isQueryExhausted()){
                        viewModel.setQueryExhausted(true)
                    }
                    listAdapter?.submitList(noteList)
                    listAdapter?.notifyDataSetChanged()
                }

                // a note been inserted or selected
                viewState.newNote?.let { newNote ->
                    navigateToDetailFragment(newNote)
                }

            }
        })

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, Observer {
            printActiveJobs()
            uiController.displayProgressBar(it)
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->
            stateMessage?.let { message ->
                if(message.response.message?.equals(DELETE_NOTE_SUCCESS) == true){
                    showUndoSnackbar_deleteNote()
                }
                else{
                    uiController.onResponseReceived(
                        response = message.response,
                        stateMessageCallback = object: StateMessageCallback {
                            override fun removeMessageFromStack() {
                                viewModel.clearStateMessage()
                            }
                        }
                    )
                }
            }
        })
    }

    private fun showUndoSnackbar_deleteNote(){
        uiController.onResponseReceived(
            response = Response(
                message = DELETE_NOTE_PENDING,
                uiComponentType = UIComponentType.SnackBar(
                    undoCallback = object : SnackbarUndoCallback {
                        override fun undo() {
                            viewModel.undoDelete()
                        }
                    },
                    onDismissCallback = object: TodoCallback {
                        override fun execute() {
                            // if the note is not restored, clear pending note
                            viewModel.setNotePendingDelete(null)
                        }
                    }
                ),
                messageType = MessageType.Info()
            ),
            stateMessageCallback = object: StateMessageCallback{
                override fun removeMessageFromStack() {
                    viewModel.clearStateMessage()
                }
            }
        )
    }

    // for debugging
    private fun printActiveJobs(){

        for((index, job) in viewModel.getActiveJobs().withIndex()){
            printLogD("NoteList",
                "${index}: ${job}")
        }
    }

    private fun navigateToDetailFragment(selectedNote: Note){
        val bundle = bundleOf(NOTE_DETAIL_SELECTED_NOTE_BUNDLE_KEY to selectedNote)
        findNavController().navigate(
            R.id.action_note_list_fragment_to_noteDetailFragment,
            bundle
        )
        viewModel.setNote(null)
    }

    private fun setupUI(){
        view?.hideKeyboard()
    }

    override fun inject() {
        getAppComponent().inject(this)
    }

    override fun onItemSelected(position: Int, item: Note) {
        if(isMultiSelectionModeEnabled()){
            viewModel.addOrRemoveNoteFromSelectedList(item)

            val selectedNotesCount = viewModel.getSelectedNotes().size
            val textViewValue = "Selected $selectedNotesCount"
            selected_notes_tv.visibility = View.VISIBLE

            if (selectedNotesCount > 0) {
                selected_notes_tv.text = textViewValue
                setIsClickable(true, 1F, true, 1F)
            } else {
                selected_notes_tv.text = FolderListFragment.NONE_SELECTED
                setIsClickable(false, 0.4F, false, 0.4F)
            }
        }
        else{
            viewModel.setNote(item)
            Log.d("action", "onItemSelected")
        }
    }

    private fun setIsClickable(
        isMoveNoteClickable: Boolean,
        moveNoteAlpha: Float,
        isDeleteNoteClickable: Boolean,
        deleteNoteAlpha: Float

    ) {
        action_move_notes.let{
            it.isClickable = isMoveNoteClickable
            it.alpha = moveNoteAlpha
        }

        action_move_notes_tv.alpha = moveNoteAlpha


        action_delete_notes.let {
            it.isClickable = isDeleteNoteClickable
            it.alpha = deleteNoteAlpha
        }

        action_delete_notes_tv.alpha = deleteNoteAlpha
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter = null // can leak memory
        itemTouchHelper = null // can leak memory
    }

    override fun isNoteSelected(note: Note): Boolean {
        return viewModel.isNoteSelected(note)
    }

    override fun onItemSwiped(position: Int) {
        if(!viewModel.isDeletePending()){
            listAdapter?.getNote(position)?.let { note ->
                viewModel.beginPendingDelete(note)
            }
        }
        else{
            listAdapter?.notifyDataSetChanged()
        }
    }

    private fun deleteNotes(){
        viewModel.setStateEvent(
            CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = DELETE_NOTES_ARE_YOU_SURE,
                        uiComponentType = UIComponentType.AreYouSureDialog(
                            object : AreYouSureCallback {
                                override fun proceed() {
                                    viewModel.deleteNotes()
                                }

                                override fun cancel() {
                                    // do nothing
                                }
                            }
                        ),
                        messageType = MessageType.Info()
                    )
                )
            )
        )
    }

    private fun logout(){
        viewModel.setStateEvent(
            CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = LOGOUT_ARE_YOU_SURE,
                        uiComponentType = UIComponentType.AreYouSureDialog(
                            object : AreYouSureCallback {
                                override fun proceed() {
                                    Firebase.auth.signOut()
                                    val intent = Intent(context, AuthActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                }

                                override fun cancel() {
                                    // do nothing
                                }
                            }
                        ),
                        messageType = MessageType.Info()
                    )
                )
            )
        )
    }

    private fun setupSearchView(){

        selected_notes_tv.visibility = View.GONE

        val searchViewToolbar: Toolbar? = toolbar_content_container
            .findViewById<Toolbar>(R.id.searchview_toolbar)

        searchViewToolbar?.let { toolbar ->
//            val searchView = toolbar.findViewById<SearchView>(R.id.search_view)
            val searchText = toolbar.findViewById<EditText>(R.id.searchText)

            searchText.addTextChangedListener(object  : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val query = searchText.getText().toString().trim()
                    Log.d("action", "onTextChanged $searchText")
                    viewModel.setQuery(query)
                            startNewSearch()
                }
            })

//            searchView.queryHint = "Search folder . . ."
//            searchView.isClickable=true
//            searchView.isIconified=true
//
//            searchView.setOnClickListener {
////                Log.d("action","search view clicked")
//                searchView.onActionViewExpanded()
//            }
//
//            val searchPlate: SearchView.SearchAutoComplete?
//                    = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
//
//            // can't use QueryTextListener in production b/c can't submit an empty string
//            when{
//                androidTestUtils.isTest() -> {
//                    searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
//                        override fun onQueryTextSubmit(query: String?): Boolean {
//                            viewModel.setQuery(query)
//                            startNewSearch()
//                            return true
//                        }
//
//                        override fun onQueryTextChange(newText: String?): Boolean {
//                            return true
//                        }
//
//                    })
//                }
//
//                else ->{
//                    searchPlate?.setOnEditorActionListener { v, actionId, _ ->
//                        if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
//                            || actionId == EditorInfo.IME_ACTION_SEARCH ) {
//                            val searchQuery = v.text.toString()
//                            viewModel.setQuery(searchQuery)
//                            startNewSearch()
//                        }
//                        true
//                    }
//                }
//            }
        }
    }

    private fun setupFAB(){
        add_new_note_fab.setOnClickListener {
            val toFolder = if (viewModel.getSelectedFolderId().isNotBlank()) viewModel.getSelectedFolderId() else NOTES

            Log.d("getSelectedFolder", "toFolder: $toFolder")

            uiController.displayInputCaptureDialog(
                getString(cho.chonotes.R.string.text_enter_a_title),
                object: DialogInputCaptureCallback{
                    override fun onTextCaptured(text: String) {
                        val newNote = viewModel.createNewNote(title = text)
                        viewModel.setStateEvent(
                            InsertNewNoteEvent(
                                title = newNote.title,
                                toFolder = toFolder
                            )
                        )
                    }
                }
            )

        }
    }

    private fun startNewSearch(){
        printLogD("DCM", "start new search")
        viewModel.clearList()
        viewModel.loadFirstPage()
    }

    private fun setupSwipeRefresh(){
        swipe_refresh.setOnRefreshListener {
            startNewSearch()
            swipe_refresh.isRefreshing = false
        }
    }

    private fun setupFilterButton(){
        val searchViewToolbar: Toolbar? = toolbar_content_container
            .findViewById<Toolbar>(R.id.searchview_toolbar)
        searchViewToolbar?.findViewById<ImageView>(R.id.action_filter)?.setOnClickListener {
            showFilterDialog()
        }

        searchViewToolbar?.findViewById<ImageView>(R.id.action_folders)?.setOnClickListener {
            findNavController().navigate(
                R.id.action_noteListFragment_to_folderListFragment
            )
        }

        searchViewToolbar?.findViewById<ImageView>(R.id.action_logout)?.setOnClickListener {
            logout()
        }
    }

    fun showFilterDialog(){

        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_filter)

            val view = dialog.getCustomView()

            val filter = viewModel.getFilter()
            val order = viewModel.getOrder()

            view.findViewById<RadioGroup>(R.id.filter_group).apply {
                when (filter) {
                    NOTE_FILTER_DATE_CREATED -> check(R.id.filter_date)
                    NOTE_FILTER_TITLE -> check(R.id.filter_title)
                }
            }

            view.findViewById<RadioGroup>(R.id.order_group).apply {
                when (order) {
                    NOTE_ORDER_ASC -> check(R.id.filter_asc)
                    NOTE_ORDER_DESC -> check(R.id.filter_desc)
                }
            }

            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {

                val newFilter =
                    when (view.findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId) {
                        R.id.filter_title -> NOTE_FILTER_TITLE
                        R.id.filter_date -> NOTE_FILTER_DATE_CREATED
                        else -> NOTE_FILTER_DATE_CREATED
                    }

                val newOrder =
                    when (view.findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId) {
                        R.id.filter_desc -> "-"
                        else -> ""
                    }

                viewModel.apply {
                    saveFilterOptions(newFilter, newOrder)
                    setNoteFilter(newFilter)
                    setNoteOrder(newOrder)
                }

                startNewSearch()

                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    companion object{
        val LOGOUT_ARE_YOU_SURE = "Do you want to logout your account?"
    }

}










































