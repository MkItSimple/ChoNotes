package cho.chonotes.framework.presentation.selectfolder

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import cho.chonotes.R
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.state.MessageType
import cho.chonotes.business.domain.state.Response
import cho.chonotes.business.domain.state.StateMessage
import cho.chonotes.business.domain.state.UIComponentType
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.framework.presentation.common.BaseNoteFragment
import cho.chonotes.framework.presentation.common.TopSpacingItemDecoration
import cho.chonotes.framework.presentation.folderlist.FOLDER_LIST_SELECTED_NOTES_BUNDLE_KEY
import cho.chonotes.framework.presentation.folderlist.FolderListViewModel
import cho.chonotes.framework.presentation.notedetail.state.NoteDetailStateEvent
import cho.chonotes.framework.presentation.notelist.NOTE_LIST_ERROR_RETRIEVEING_SELECTED_FOLDER
import cho.chonotes.framework.presentation.notelist.NOTE_LIST_PREVIOUS_FRAGMENT_BUNDLE_KEY
import cho.chonotes.framework.presentation.notelist.NOTE_LIST_SELECTED_FOLDER_BUNDLE_KEY
import cho.chonotes.framework.presentation.notelist.NOTE_LIST_SELECTED_NOTES_BUNDLE_KEY
import cho.chonotes.util.AndroidTestUtils
import kotlinx.android.synthetic.main.fragment_folder_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class SelectFolderFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
) : BaseNoteFragment(R.layout.fragment_select_folder),
    SelectFolderAdapter.Interaction
{
    @Inject
    lateinit var androidTestUtils: AndroidTestUtils

    val viewModel: FolderListViewModel by viewModels {
        viewModelFactory
    }

    private var listAdapter: SelectFolderAdapter? = null
    private var itemTouchHelper: ItemTouchHelper? = null

    private var selectedNotes: ArrayList<Note>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun clearArgs(){
        arguments?.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        subscribeObservers()
        setupSelectView()

        //  Get selected notes to move
        getSelectedNotesFromNoteListFragment()
    }

    // added
    private fun getSelectedNotesFromNoteListFragment() {
        arguments?.let { args ->
            (args.getParcelableArrayList<Note>(FOLDER_LIST_SELECTED_NOTES_BUNDLE_KEY) as ArrayList<Note>?)?.let { notes ->
                selectedNotes = notes
            }
        }
//        clearArgs()
    }

    private fun setupSelectView() {
        view?.let { v ->
            val view = View.inflate(
                v.context,
                R.layout.layout_selectfolder_toolbar,
                null
            )
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            toolbar_content_container.addView(view)
            setupToolbar(view)
        }
    }

    private fun setupToolbar(parentView: View) {
        parentView
            .findViewById<ImageView>(R.id.action_exit)
            .setOnClickListener {
                navigateBackToNoteListFragment()
            }
    }

    private fun navigateBackToNoteListFragment() {
        findNavController().navigate(
            R.id.action_selectFolderFragment_to_noteListFragment
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveNumFoldersInCache()
        viewModel.clearList()
        viewModel.refreshSearchQuery()
    }

    private fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState ->

            if(viewState != null){
                viewState.folderList?.let { folderList ->
                    if(viewModel.isPaginationExhausted()
                        && !viewModel.isQueryExhausted()){
                        viewModel.setQueryExhausted(true)
                    }
                    listAdapter?.submitList(folderList)
                    listAdapter?.notifyDataSetChanged()
                }

            }
        })
    }

    private fun setupRecyclerView() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecorator = TopSpacingItemDecoration(20)
            addItemDecoration(topSpacingDecorator)

            listAdapter = SelectFolderAdapter(
                this@SelectFolderFragment,
                viewLifecycleOwner,
                viewModel.folderListInteractionManager.selectedFolders,
                dateUtil
            )

            adapter = listAdapter
        }
    }

    override fun inject() {
        getAppComponent().inject(this)
    }

    override fun onItemSelected(position: Int, item: Folder) {
        Log.d("action", "folder: ${item.folder_id} selected")
        val bundle = bundleOf(
            NOTE_LIST_SELECTED_FOLDER_BUNDLE_KEY to item,
            NOTE_LIST_SELECTED_NOTES_BUNDLE_KEY to selectedNotes,
            NOTE_LIST_PREVIOUS_FRAGMENT_BUNDLE_KEY to THIS_FRAGMENT_NAME
        )
        findNavController().navigate(
            R.id.action_selectFolderFragment_to_noteListFragment,
            bundle
        )
    }

    override fun restoreListPosition() {
        Log.d("action", "restoreListPosition")
    }

    companion object {
        val THIS_FRAGMENT_NAME = "SelectFolderFragment"
    }
}