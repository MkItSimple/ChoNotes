package cho.chonotes.framework.presentation.selectfolder

import android.content.SharedPreferences
import android.os.Parcelable
import androidx.lifecycle.LiveData
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.model.FolderFactory
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.interactors.folderlist.DeleteMultipleFolders.Companion.DELETE_FOLDERS_YOU_MUST_SELECT
import cho.chonotes.business.interactors.folderlist.FolderListInteractors
import cho.chonotes.framework.datasource.cache.database.FOLDER_FILTER_DATE_CREATED
import cho.chonotes.framework.datasource.cache.database.FOLDER_ORDER_DESC
import cho.chonotes.framework.datasource.preferences.PreferenceKeys.Companion.FOLDER_FILTER
import cho.chonotes.framework.datasource.preferences.PreferenceKeys.Companion.FOLDER_ORDER
import cho.chonotes.framework.presentation.common.BaseViewModel
import cho.chonotes.framework.presentation.folderlist.state.FolderListInteractionManager
import cho.chonotes.framework.presentation.folderlist.state.FolderListStateEvent.*
import cho.chonotes.framework.presentation.folderlist.state.FolderListToolbarState
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState.*
import cho.chonotes.util.printLogD
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

const val DELETE_PENDING_ERROR = "There is already a pending delete operation."
const val FOLDER_PENDING_DELETE_BUNDLE_KEY = "pending_delete"
const val FOLDER_LIST_SELECTED_NOTES_COUNT_BUNDLE_KEY = "selectedNote" // added

@ExperimentalCoroutinesApi
@FlowPreview
@Singleton
class SelectFolderViewModel
@Inject
constructor(
    private val folderInteractors: FolderListInteractors,
    private val folderFactory: FolderFactory,
    private val editor: SharedPreferences.Editor,
    sharedPreferences: SharedPreferences
): BaseViewModel<FolderListViewState>(){

    val folderListInteractionManager =
        FolderListInteractionManager()

    val uid = FirebaseAuth.getInstance().currentUser!!.uid

    override fun handleNewData(data: FolderListViewState) {
        data.let { viewState ->
            viewState.folder?.let {

            }
        }
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        val job: Flow<DataState<FolderListViewState>?> = when(stateEvent){

            is InsertNewFolderEvent -> {
                folderInteractors.insertNewFolder.insertNewFolder(
                    folder_name = stateEvent.folder_name,
                    uid = uid,
                    stateEvent = stateEvent
                )
            }

            is SearchFoldersEvent -> {
                if(stateEvent.clearLayoutManagerState){
                    clearLayoutManagerState()
                }
                folderInteractors.searchFolders.searchFolders(
                    uid = uid,
                    query = getSearchQuery(),
                    filterAndOrder = getOrder() + getFilter(),
                    page = getPage(),
                    stateEvent = stateEvent
                )
            }

            else -> {
                emitInvalidStateEvent(stateEvent)
            }
        }
        launchJob(stateEvent, job)
    }

    fun clearLayoutManagerState(){
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = null
        setViewState(update)
    }

    fun getSearchQuery(): String {
        return getCurrentViewStateOrNew().searchQuery
            ?: return ""
    }

    fun getOrder(): String {
        return getCurrentViewStateOrNew().order
            ?: FOLDER_ORDER_DESC
    }

    fun getFilter(): String {
        return getCurrentViewStateOrNew().filter
            ?: FOLDER_FILTER_DATE_CREATED
    }

    private fun getPage(): Int{
        return getCurrentViewStateOrNew().page
            ?: return 1
    }


    override fun initNewViewState(): FolderListViewState {
        TODO("Not yet implemented")
    }
}