package cho.chonotes.framework.presentation.folderlist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import cho.chonotes.R
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.business.interactors.common.DeleteFolder.Companion.DELETE_FOLDER_PENDING
import cho.chonotes.business.interactors.common.DeleteFolder.Companion.DELETE_FOLDER_SUCCESS
import cho.chonotes.business.interactors.folderlist.DeleteMultipleFolders.Companion.DELETE_FOLDERS_ARE_YOU_SURE
import cho.chonotes.framework.datasource.cache.database.FOLDER_FILTER_DATE_CREATED
import cho.chonotes.framework.datasource.cache.database.FOLDER_FILTER_TITLE
import cho.chonotes.framework.datasource.cache.database.FOLDER_ORDER_ASC
import cho.chonotes.framework.datasource.cache.database.FOLDER_ORDER_DESC
import cho.chonotes.framework.presentation.common.BaseNoteFragment
import cho.chonotes.framework.presentation.common.TopSpacingItemDecoration
import cho.chonotes.framework.presentation.common.hideKeyboard
import cho.chonotes.framework.presentation.folderlist.state.FolderListStateEvent.*
import cho.chonotes.framework.presentation.folderlist.state.FolderListToolbarState.*
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState
import cho.chonotes.framework.presentation.notelist.NOTE_LIST_PREVIOUS_FRAGMENT_BUNDLE_KEY
import cho.chonotes.framework.presentation.notelist.NOTE_LIST_SELECTED_FOLDER_BUNDLE_KEY
import cho.chonotes.util.AndroidTestUtils
import cho.chonotes.util.TodoCallback
import cho.chonotes.util.printLogD
import kotlinx.android.synthetic.main.fragment_folder_list.*
import kotlinx.coroutines.*
import javax.inject.Inject

const val FOLDER_LIST_STATE_BUNDLE_KEY = "cho.chonotes.notes.framework.presentation.folderlist.state"

@FlowPreview
@ExperimentalCoroutinesApi
class FolderListFragment
constructor(
        private val viewModelFactory: ViewModelProvider.Factory,
        private val dateUtil: DateUtil
) : BaseNoteFragment(R.layout.fragment_folder_list),
        FolderListAdapter.Interaction,
        ItemTouchHelperAdapter
{
        @Inject
        lateinit var androidTestUtils: AndroidTestUtils

        val viewModel: FolderListViewModel by viewModels {
                viewModelFactory
        }

        private var listAdapter: FolderListAdapter? = null
        private var itemTouchHelper: ItemTouchHelper? = null
        private var selectedFoldersCount: Int? = null

//        private val searchViewToolbar: Toolbar? = toolbar_content_container
//                .findViewById<Toolbar>(R.id.searchview_toolbar)
//
//        private val multiselectToolbar: Toolbar? = toolbar_content_container
//                .findViewById<Toolbar>(R.id.multiselect_toolbar)
//
////        private val MultiSelectToolbar: Toolbar? = toolbar_content_container
////                .findViewById<Toolbar>(R.id.multiselect_toolbar)

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                viewModel.setupChannel()
                arguments?.let { args ->
                        args.getParcelable<Folder>(FOLDER_PENDING_DELETE_BUNDLE_KEY)?.let { note ->
                                viewModel.setFolderPendingDelete(note)
                                showUndoSnackBarDeleteFolder()
                                clearArgs()
                        }
                }
        }

        private fun showUndoSnackBarDeleteFolder(){
                uiController.onResponseReceived(
                        response = Response(
                                message = DELETE_FOLDER_PENDING,
                                uiComponentType = UIComponentType.SnackBar(
                                        undoCallback = object : SnackbarUndoCallback {
                                                override fun undo() {
                                                        viewModel.undoDelete()
                                                }
                                        },
                                        onDismissCallback = object: TodoCallback {
                                                override fun execute() {
                                                        // if the folder is not restored, clear pending folder
                                                        viewModel.setFolderPendingDelete(null)
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
                restoreInstanceState(savedInstanceState)


        }

        private fun restoreInstanceState(savedInstanceState: Bundle?) {
                savedInstanceState?.let { inState ->
                        (inState[FOLDER_LIST_STATE_BUNDLE_KEY] as FolderListViewState?)?.let { viewState ->
                                viewModel.setViewState(viewState)
                        }
                }
        }

        override fun onResume() {
                super.onResume()
                viewModel.retrieveNumFoldersInCache()
                viewModel.clearList()
                viewModel.refreshSearchQuery()
                viewModel.refreshSearchQueryWithNotes()
        }

        override fun onPause() {
                super.onPause()
                saveLayoutManagerState()
        }

        private fun navigateToNoteListFragment(selectedFolder: Folder){
                val bundle = bundleOf(
                        NOTE_LIST_SELECTED_FOLDER_BUNDLE_KEY to selectedFolder,
                        NOTE_LIST_PREVIOUS_FRAGMENT_BUNDLE_KEY to THIS_FRAGMENT_NAME
                )
                findNavController().navigate(
                        R.id.action_folderListFragment_to_noteListFragment,
                        bundle
                )
                viewModel.setFolder(null)
        }

        private fun setupUI(){
                view?.hideKeyboard()
        }

        override fun inject() {
                getAppComponent().inject(this)
        }

        private fun saveLayoutManagerState(){
                recycler_view.layoutManager?.onSaveInstanceState()?.let { lmState ->
                        viewModel.setLayoutManagerState(lmState)
                }
        }

        private fun subscribeObservers() {

//                viewModel.foldersWithNotes.observe(viewLifecycleOwner, Observer { foldersWithNotes ->
//                        if (foldersWithNotes.isNotEmpty()){
//                        } else {
//                                viewModel.setStateEvent(
//                                        InsertDefaultFolderEvent()
//                                )
//                        }
//                })


                viewModel.toolbarState.observe(viewLifecycleOwner, Observer{ toolbarState ->
                        when(toolbarState){

                                is MultiSelectionState -> {
                                        enableMultiSelectToolbarState()
                                        disableSearchViewToolbarState()
                                        Log.d("action", "enableMultiSelectToolbarState")
                                }

                                is SearchViewState -> {
                                        enableSearchViewToolbarState()
                                        disableMultiSelectToolbarState()
                                }
                        }
                })

                viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState ->

                        if(viewState != null){
//                                viewState.folderWithNotesCacheEntityList?.let { folderWithNotesList ->
//                                        val folderWithNotesListSize = folderWithNotesList.size
//
//                                        for (i in 0..folderWithNotesList.size) {
//                                                val folderName = folderWithNotesList[i].folderCacheEntity.folder_name
//                                                Log.d("folderName", "folderName: $folderName")
//                                        }
//                                        Log.d("folderList", "folderWithNotesList: $folderWithNotesList")
//                                }

//                                val folderList = viewState.folderList
//                                val folderWithNotesList = viewState.folderWithNotesList
//
////                                Log.d("lists", "folderList: $folderList")
////                                Log.d("lists", "folderWithNotesList: $folderWithNotesList")


                                viewState.folderList?.let { folderList ->
                                        Log.d("folderList", "folderList: $folderList")
                                        if(viewModel.isPaginationExhausted()
                                                && !viewModel.isQueryExhausted()){
                                                viewModel.setQueryExhausted(true)
                                        }
                                        listAdapter?.submitList(folderList)
                                        listAdapter?.notifyDataSetChanged()
                                }

                                // a folder been inserted or selected
                                viewState.newFolder?.let { newFolder ->
                                        navigateToNoteListFragment(newFolder)
                                }

                        }
                })

                viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, Observer {
                        //printActiveJobs()
                        uiController.displayProgressBar(it)
                })

                viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->
                        stateMessage?.let { message ->
                                if(message.response.message?.equals(DELETE_FOLDER_SUCCESS) == true){
                                        showUndoSnackBarDeleteFolder()
                                }
                                else{
                                        uiController.onResponseReceived(
                                                response = message.response,
                                                stateMessageCallback = object:
                                                        StateMessageCallback {
                                                        override fun removeMessageFromStack() {
                                                                viewModel.clearStateMessage()
                                                                viewModel.refreshSearchQuery()
                                                        }
                                                }
                                        )
                                }
                        }
                })
        }

        private fun setupRecyclerView() {
                recycler_view.apply {
                        layoutManager = LinearLayoutManager(activity)
                        val topSpacingDecorator = TopSpacingItemDecoration(20)
                        addItemDecoration(topSpacingDecorator)
                        itemTouchHelper = ItemTouchHelper(
                                FolderItemTouchHelperCallback(
                                        this@FolderListFragment,
                                        viewModel.folderListInteractionManager
                                )
                        )
                        listAdapter = FolderListAdapter(
                                this@FolderListFragment,
                                viewLifecycleOwner,
                                viewModel.folderListInteractionManager.selectedFolders,
                                dateUtil
                        )
//                        itemTouchHelper?.attachToRecyclerView(this)

                        adapter = listAdapter
                }
        }

        private fun enableMultiSelectToolbarState(){
                view?.let { v ->
                        val view = View.inflate(
                                v.context,
                                R.layout.layout_multiselection_toolbar_folder,
                                null
                        )
                        view.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        toolbar_content_container.addView(view)
                        setupMultiSelectionToolbar(view)
                }
        }

        private fun setupMultiSelectionToolbar(parentView: View){
                constraint_bottom.visibility = View.VISIBLE
                add_new_note_fab.visibility = View.GONE

                parentView
                        .findViewById<ImageView>(R.id.action_exit_multiselect_state)
                        .setOnClickListener {
                                viewModel.setToolbarState(SearchViewState())
                        }

//                parentView
//                        .findViewById<ImageView>(R.id.action_delete_folders)
//                        .setOnClickListener {
//                                deleteFolders()
//                        }

                action_delete_folders.setOnClickListener {
                        showDeleteDialog()
//                        deleteFolders()
                }

                action_rename_folder.setOnClickListener {

                        uiController.displayInputCaptureDialog(
                                getString(cho.chonotes.R.string.text_enter_folder_name),
                                object: DialogInputCaptureCallback {
                                        override fun onTextCaptured(text: String) {
//                                              val newFolder = viewModel.createNewFolder(folder_name = text)
                                                val selectedFolder = viewModel.getSelectedFolders()[0]
                                                Log.d("rename", "rename: $selectedFolder to $text")
                                                viewModel.setStateEvent(
                                                        RenameFolderEvent(
                                                                selectedFolder = selectedFolder,
                                                                newFolderName = text
                                                        )
                                                )
                                        }
                                }
                        )
                }
        }

        override fun onItemSelected(position: Int, item: Folder) {

                if(isMultiSelectionModeEnabled()){
                        if (item.folder_id != "notes") {
                                viewModel.addOrRemoveFolderFromSelectedList(item)
                                val selectedFoldersCount = viewModel.getSelectedFolders().size
                                val textViewValue = "Selected ${selectedFoldersCount.toString()}"
                                select_folder_tv.let { textView ->
                                        textView.visibility = View.VISIBLE
                                }

                                when {
                                        selectedFoldersCount == 0 -> {
                                                select_folder_tv.text = NONE_SELECTED
                                                setIsClickable(false, 0.4F, false, 0.4F)
                                        }
                                        selectedFoldersCount == 1 ->{
                                                select_folder_tv.text = textViewValue
                                                setIsClickable(true, 1F, true, 1F)
                                        }
                                        selectedFoldersCount > 1 ->{
                                                select_folder_tv.text = textViewValue
                                                setIsClickable(true, 1F, false, 0.4F)
                                        }
                                        else -> {
                                                // do nothing . . comment
                                        }
                                }
                        }
                }
                else{
                        viewModel.setFolder(item)
                }
        }

        private fun setIsClickable(
                isDeleteFolderClickable: Boolean,
                deleteFolderAlpha: Float,
                isRenameFolderClickable: Boolean,
                renameFolderAlpha: Float
        ) {
                action_delete_folders.let{
                        it.isClickable = isDeleteFolderClickable
                        it.alpha = deleteFolderAlpha
                }

                action_delete_folders_tv.alpha = deleteFolderAlpha


                action_rename_folder.let {
                        it.isClickable = isRenameFolderClickable
                        it.alpha = renameFolderAlpha
                }

                action_rename_folder_tv.alpha = renameFolderAlpha
        }

        private fun deleteFolders(deleteFolderAndNotes: String) {


                viewModel.setStateEvent(
                        CreateStateMessageEvent(
                                stateMessage = StateMessage(
                                        response = Response(
                                                message = DELETE_FOLDERS_ARE_YOU_SURE,
                                                uiComponentType = UIComponentType.AreYouSureDialog(
                                                        object : AreYouSureCallback {
                                                                override fun proceed() {
//                                                                        viewModel.deleteFolders()
//                                                                        if (deleteFolderAndNotes == DELETE_FOLDER_AND_NOTES) {
////                                                                                Log.d("delete", "delete folder and notes")
//                                                                                viewModel.deleteFoldersAndNotes()
//                                                                        } else {
////                                                                                Log.d("delete", "delete folder only")
                                                                                viewModel.deleteFolders(deleteFolderAndNotes)
//                                                                        }
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

        private fun enableSearchViewToolbarState(){
                view?.let { v ->
                        val view = View.inflate(
                                v.context,
                                R.layout.layout_searchview_toolbar_folder,
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

        private fun setupSearchView(){

                select_folder_tv.visibility = View.GONE
                constraint_bottom.visibility = View.GONE
                add_new_note_fab.visibility = View.VISIBLE

                val searchViewToolbar: Toolbar? = toolbar_content_container
                        .findViewById<Toolbar>(R.id.searchview_toolbar)

                searchViewToolbar?.let { toolbar ->

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
                }

        }




        private fun disableMultiSelectToolbarState(){
                view?.let {
                        val view = toolbar_content_container
                                .findViewById<Toolbar>(R.id.multiselect_toolbar)
                        toolbar_content_container.removeView(view)
                        viewModel.clearSelectedFolders()
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

        private fun setupFAB(){
                add_new_note_fab.setOnClickListener {

                        uiController.displayInputCaptureDialog(
                                getString(cho.chonotes.R.string.text_enter_folder_name),
                                object: DialogInputCaptureCallback {
                                        override fun onTextCaptured(text: String) {
                                                val newFolder = viewModel.createNewFolder(folder_name = text)
                                                viewModel.setStateEvent(
                                                        InsertNewFolderEvent(
                                                                folder_name = newFolder.folder_name
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
        }

        private fun showFilterDialog() {
                activity?.let {
                        val dialog = MaterialDialog(it)
                                .noAutoDismiss()
                                .customView(R.layout.layout_filter)

                        val view = dialog.getCustomView()

                        val filter = viewModel.getFilter()
                        val order = viewModel.getOrder()

                        view.findViewById<RadioGroup>(R.id.filter_group).apply {
                                when (filter) {
                                        FOLDER_FILTER_DATE_CREATED -> check(R.id.filter_date)
                                        FOLDER_FILTER_TITLE -> check(R.id.filter_title)
                                }
                        }

                        view.findViewById<RadioGroup>(R.id.order_group).apply {
                                when (order) {
                                        FOLDER_ORDER_ASC -> check(R.id.filter_asc)
                                        FOLDER_ORDER_DESC -> check(R.id.filter_desc)
                                }
                        }

                        view.findViewById<TextView>(R.id.positive_button).setOnClickListener {

                                val newFilter =
                                        when (view.findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId) {
                                                R.id.filter_title -> FOLDER_FILTER_TITLE
                                                R.id.filter_date -> FOLDER_FILTER_DATE_CREATED
                                                else -> FOLDER_FILTER_DATE_CREATED
                                        }

                                val newOrder =
                                        when (view.findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId) {
                                                R.id.filter_desc -> "-"
                                                else -> ""
                                        }

                                viewModel.apply {
                                        saveFilterOptions(newFilter, newOrder)
                                        setFolderFilter(newFilter)
                                        setFolderOrder(newOrder)
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

        fun showDeleteDialog(){
                activity?.let {
                        val dialog = MaterialDialog(it)
                                .noAutoDismiss()
                                .customView(R.layout.layout_delete)

                        val view = dialog.getCustomView()

                        view.findViewById<TextView>(R.id.delete_folder_and_notes).setOnClickListener {
                                deleteFolders(DELETE_FOLDER_AND_NOTES)
                                dialog.dismiss()
                        }

                        view.findViewById<TextView>(R.id.delete_folder).setOnClickListener {
                                deleteFolders(DELETE_FOLDER_ONLY)
                                dialog.dismiss()
                        }

                        view.findViewById<TextView>(R.id.delete_cancel).setOnClickListener {
                                dialog.dismiss()
                        }

                        dialog.show()
                }
        }


        override fun restoreListPosition() {
                viewModel.getLayoutManagerState()?.let { lmState ->
                        recycler_view?.layoutManager?.onRestoreInstanceState(lmState)
                }
        }

        override fun isFolderSelected(folder: Folder): Boolean {
//                return viewModel.isFolderSelected(folder)
                return true
        }

        override fun onItemSwiped(position: Int) {

                // && position == 0 . . item on position 0 cannot be swipped
                listAdapter?.getFolder(position)?.let { folder ->
                        if(!viewModel.isDeletePending() && folder.folder_id != NOTES){
                                viewModel.beginPendingDelete(folder)
                        } else{
                                listAdapter?.notifyDataSetChanged()
                        }
                }


        }

        companion object {
                val THIS_FRAGMENT_NAME = "FolderListFragment"
                val NOTES = "notes"
                val NONE_SELECTED = "None selected"
                val DELETE_FOLDER_AND_NOTES = "DELETE FOLDER AND NOTES"
                val DELETE_FOLDER_ONLY = "DELETE FOLDER ONLY"
        }
}