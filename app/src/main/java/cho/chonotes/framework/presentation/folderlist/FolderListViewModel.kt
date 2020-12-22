package cho.chonotes.framework.presentation.folderlist

import android.content.SharedPreferences
import android.os.Parcelable
import android.util.Log
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
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState.FolderPendingDelete
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

const val DELETE_PENDING_ERROR = "There is already a pending delete operation."
const val FOLDER_PENDING_DELETE_BUNDLE_KEY = "pending_delete"
const val FOLDER_LIST_SELECTED_NOTES_BUNDLE_KEY = "selectedNote"
const val DELETE_FOLDER_AND_NOTES = "DELETE FOLDER AND NOTES"

@ExperimentalCoroutinesApi
@FlowPreview
@Singleton
class FolderListViewModel
@Inject
constructor(
    private val folderInteractors: FolderListInteractors,
    private val folderFactory: FolderFactory,
    private val editor: SharedPreferences.Editor,
    sharedPreferences: SharedPreferences
): BaseViewModel<FolderListViewState>(){

    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val folderListInteractionManager =
        FolderListInteractionManager()

    val toolbarState: LiveData<FolderListToolbarState>
        get() = folderListInteractionManager.toolbarState

    init {
        setFolderFilter(
            sharedPreferences.getString(
                FOLDER_FILTER,
                FOLDER_FILTER_DATE_CREATED
            )
        )
        setFolderOrder(
            sharedPreferences.getString(
                FOLDER_ORDER,
                FOLDER_ORDER_DESC
            )
        )
    }

    override fun handleNewData(data: FolderListViewState) {

        data.let { viewState ->

            viewState.folderWithNotesCacheEntityList?.let { folderWithNotesList ->
                if (folderWithNotesList.isEmpty()){
                    setStateEvent(InsertDefaultFolderEvent())
                }
                setFolderListData(folderWithNotesList)
            }

            viewState.numFoldersInCache?.let { numFolders ->
                setNumFoldersInCache(numFolders)
            }

            viewState.newFolder?.let { folder ->
                Log.d("folder", "folder: $folder")
            }

            viewState.folderPendingDelete?.let { restoredFolder ->
                restoredFolder.folder?.let { folder ->
                    setRestoredFolderId(folder)
                }
                setFolderPendingDelete(null)
            }
        }

    }

    override fun setStateEvent(stateEvent: StateEvent) {

        val job: Flow<DataState<FolderListViewState>?> = when(stateEvent){

            is InsertDefaultFolderEvent -> {
                folderInteractors.insertDefaultFolder.insertDefaultFolder(
                    uid = uid,
                    stateEvent = stateEvent
                )
            }

            is RenameFolderEvent -> {
                folderInteractors.renameFolder.renameFolder(
                    selectedFolder = stateEvent.selectedFolder,
                    newFolderName = stateEvent.newFolderName,
                    newUID = uid,
                    stateEvent = stateEvent
                )
            }

            is InsertNewFolderEvent -> {
                Log.d("choCalled","InsertNewFolderEvent")
                folderInteractors.insertNewFolder.insertNewFolder(
                    folder_name = stateEvent.folder_name,
                    uid = uid,
                    stateEvent = stateEvent
                )
            }

            is InsertMultipleFoldersEvent -> {
                folderInteractors.insertMultipleFolders.insertFolders(
                    numFolders = stateEvent.numFolders,
                    stateEvent = stateEvent
                )
            }

            is DeleteFolderEvent -> {
                folderInteractors.deleteFolder.deleteFolder(
                    folder = stateEvent.folder,
                    stateEvent = stateEvent
                )
            }

            is DeleteMultipleFoldersEvent -> {
                folderInteractors.deleteMultipleFolders.deleteFolders(
                    folders = stateEvent.folders,
                    stateEvent = stateEvent
                )
            }

            is DeleteMultipleFoldersAndNotesEvent -> {
                folderInteractors.deleteMultipleFoldersAndNotes.deleteFoldersAndNotes(
                    folders = stateEvent.folders,
                    stateEvent = stateEvent
                )
            }


            is RestoreDeletedFolderEvent -> {
                folderInteractors.restoreDeletedFolder.restoreDeletedFolder(
                    folder = stateEvent.folder,
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

            is SearchFoldersWithNotesEvent -> {
                if(stateEvent.clearLayoutManagerState){
                    clearLayoutManagerState()
                }
                folderInteractors.searchFoldersWithNotes.searchFoldersWithNotes(
                    uid = uid,
                    query = getSearchQuery(),
                    filterAndOrder = getOrder() + getFilter(),
                    page = getPage(),
                    stateEvent = stateEvent
                )
            }

            is GetNumFoldersInCacheEvent -> {
                folderInteractors.getNumFolders.getNumFolders(
                    stateEvent = stateEvent
                )
            }

            is CreateStateMessageEvent -> {
                emitStateMessageEvent(
                    stateMessage = stateEvent.stateMessage,
                    stateEvent = stateEvent
                )
            }

            else -> {
                emitInvalidStateEvent(stateEvent)
            }
        }
        launchJob(stateEvent, job)
    }

    private fun removeSelectedFoldersFromList(){
        val update = getCurrentViewStateOrNew()
        update.folderList?.removeAll(getSelectedFolders())
        setViewState(update)
        clearSelectedFolders()
    }

    fun deleteFolders(deleteFolderAndNotes: String) {
        if(getSelectedFolders().size > 0){

            if (deleteFolderAndNotes == DELETE_FOLDER_AND_NOTES) {
                setStateEvent(DeleteMultipleFoldersAndNotesEvent(getSelectedFolders()))
            } else {
                setStateEvent(DeleteMultipleFoldersEvent(getSelectedFolders()))
            }

            removeSelectedFoldersFromList()
        }
        else{
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = DELETE_FOLDERS_YOU_MUST_SELECT,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Info()
                        )
                    )
                )
            )
        }
    }

    fun getSelectedFolders() = folderListInteractionManager.getSelectedFolders()

    fun setToolbarState(state: FolderListToolbarState)
            = folderListInteractionManager.setToolbarState(state)

    fun isMultiSelectionStateActive()
            = folderListInteractionManager.isMultiSelectionStateActive()

    override fun initNewViewState(): FolderListViewState {
        return FolderListViewState()
    }

    fun getFilter(): String {
        return getCurrentViewStateOrNew().filter
            ?: FOLDER_FILTER_DATE_CREATED
    }

    fun getOrder(): String {
        return getCurrentViewStateOrNew().order
            ?: FOLDER_ORDER_DESC
    }

    fun getSearchQuery(): String {
        return getCurrentViewStateOrNew().searchQuery
            ?: return ""
    }

    private fun getPage(): Int{
        return getCurrentViewStateOrNew().page
            ?: return 1
    }

    private fun setFolderListData(foldersList: ArrayList<Folder>){
        val update = getCurrentViewStateOrNew()
        update.folderList = foldersList
        setViewState(update)
    }

    fun setQueryExhausted(isExhausted: Boolean){
        val update = getCurrentViewStateOrNew()
        update.isQueryExhausted = isExhausted
        setViewState(update)
    }

    fun setFolder(folder: Folder?){
        val update = getCurrentViewStateOrNew()
        update.newFolder = folder
        setViewState(update)
    }

    fun setQuery(query: String?){
        val update =  getCurrentViewStateOrNew()
        update.searchQuery = query
        setViewState(update)
    }

    private fun setRestoredFolderId(restoredFolder: Folder){
        val update = getCurrentViewStateOrNew()
        update.folderList?.let { folderList ->
            for((index, folder) in folderList.withIndex()){
                if(folder.folder_name.equals(restoredFolder.folder_name)){
                    folderList.remove(folder)
                    folderList.add(index, restoredFolder)
                    update.folderList = folderList
                    break
                }
            }
        }
        setViewState(update)
    }

    fun isDeletePending(): Boolean{
        val pendingFolder = getCurrentViewStateOrNew().folderPendingDelete
        if(pendingFolder != null){
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = DELETE_PENDING_ERROR,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Info()
                        )
                    )
                )
            )
            return true
        }
        else{
            return false
        }
    }

    fun beginPendingDelete(folder: Folder){
        setFolderPendingDelete(folder)
        removePendingFolderFromList(folder)
        setStateEvent(
            DeleteFolderEvent(
                folder = folder
            )
        )
    }

    private fun removePendingFolderFromList(folder: Folder?){
        val update = getCurrentViewStateOrNew()
        val list = update.folderList
        if(list?.contains(folder) == true){
            list.remove(folder)
            update.folderList = list
            setViewState(update)
        }
    }

    fun undoDelete(){
        val update = getCurrentViewStateOrNew()
        update.folderPendingDelete?.let { folder ->
            if(folder.listPosition != null && folder.folder != null){
                update.folderList?.add(
                    folder.listPosition as Int,
                    folder.folder as Folder
                )
                setStateEvent(RestoreDeletedFolderEvent(folder.folder as Folder))
            }
        }
        setViewState(update)
    }


    fun setFolderPendingDelete(folder: Folder?){
        val update = getCurrentViewStateOrNew()
        if(folder != null){
            update.folderPendingDelete = FolderPendingDelete(
                folder = folder,
                listPosition = findListPositionOfFolder(folder)
            )
        }
        else{
            update.folderPendingDelete = null
        }
        setViewState(update)
    }

    private fun findListPositionOfFolder(folder: Folder?): Int {
        val viewState = getCurrentViewStateOrNew()
        viewState.folderList?.let { folderList ->
            for((index, item) in folderList.withIndex()){
                if(item.folder_id == folder?.folder_id){
                    return index
                }
            }
        }
        return 0
    }

    private fun setNumFoldersInCache(numFolders: Int){
        val update = getCurrentViewStateOrNew()
        update.numFoldersInCache = numFolders
        setViewState(update)
    }

    fun createNewFolder(
        id: String? = null,
        folder_name: String,
        notes_count: Int? = null
    ) = folderFactory.createSingleFolder(id, folder_name, notes_count, uid = "")

    fun getFolderListSize() = getCurrentViewStateOrNew().folderList?.size?: 0

    private fun getNumFoldersInCache() = getCurrentViewStateOrNew().numFoldersInCache?: 0

    fun isPaginationExhausted() = getFolderListSize() >= getNumFoldersInCache()

    private fun resetPage(){
        val update = getCurrentViewStateOrNew()
        update.page = 1
        setViewState(update)
    }

    fun isQueryExhausted(): Boolean{
        return getCurrentViewStateOrNew().isQueryExhausted?: true
    }

    fun clearList(){
        val update = getCurrentViewStateOrNew()
        update.folderList = ArrayList()
        setViewState(update)
    }

    fun loadFirstPage() {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(SearchFoldersWithNotesEvent())
    }

    fun nextPage(){
        if(!isQueryExhausted()){
            clearLayoutManagerState()
            incrementPageNumber()
            setStateEvent(SearchFoldersWithNotesEvent())
        }
    }

    private fun incrementPageNumber(){
        val update = getCurrentViewStateOrNew()
        val page = update.copy().page ?: 1
        update.page = page.plus(1)
        setViewState(update)
    }

    fun retrieveNumFoldersInCache(){
        setStateEvent(GetNumFoldersInCacheEvent())
    }

    fun refreshSearchQuery(){
        setQueryExhausted(false)
        setStateEvent(SearchFoldersWithNotesEvent(false))
    }

    fun refreshSearchQueryWithNotes(){
        setQueryExhausted(false)
        setStateEvent(SearchFoldersWithNotesEvent(false))
    }

    fun getLayoutManagerState(): Parcelable? {
        return getCurrentViewStateOrNew().layoutManagerState
    }

    fun setLayoutManagerState(layoutManagerState: Parcelable){
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = layoutManagerState
        setViewState(update)
    }

    fun clearLayoutManagerState(){
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = null
        setViewState(update)
    }

    fun addOrRemoveFolderFromSelectedList(folder: Folder)
            = folderListInteractionManager.addOrRemoveFolderFromSelectedList(folder)

    fun isFolderSelected(folder: Folder): Boolean
            = folderListInteractionManager.isFolderSelected(folder)

    fun clearSelectedFolders() = folderListInteractionManager.clearSelectedFolders()

    fun setFolderFilter(filter: String?){
        filter?.let{
            val update = getCurrentViewStateOrNew()
            update.filter = filter
            setViewState(update)
        }
    }

    fun setFolderOrder(order: String?){
        val update = getCurrentViewStateOrNew()
        update.order = order
        setViewState(update)
    }

    fun saveFilterOptions(filter: String, order: String){
        editor.putString(FOLDER_FILTER, filter)
        editor.apply()

        editor.putString(FOLDER_ORDER, order)
        editor.apply()
    }
}