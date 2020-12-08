package cho.chonotes.business.interactors.folderlist

import android.util.Log
import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.FolderCacheDataSource
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.network.abstraction.FolderNetworkDataSource
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.data.util.safeApiCall
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.interactors.common.DeleteFolder
import cho.chonotes.business.interactors.common.DeleteFolder.Companion.DEFAULT_FOLDER_ID
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteMultipleFoldersAndNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val folderCacheDataSource: FolderCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val folderNetworkDataSource: FolderNetworkDataSource
){

    // set true if an error occurs when deleting any of the folders from cache
    private var onDeleteError: Boolean = false

    /**
     * Logic:
     * 1. execute all the deletes and save result into an ArrayList<DataState<FolderListViewState>>
     * 2a. If one of the results is a failure, emit an "error" response
     * 2b. If all success, emit success response
     * 3. Update network with folders that were successfully deleted
     */
    fun deleteFoldersAndNotes(
        folders: List<Folder>,
        stateEvent: StateEvent
    ): Flow<DataState<FolderListViewState>?> = flow {
        val cachedNotesList = getCachedNotesByFolderId(folders)

        val successfulDeletes: ArrayList<Folder> = ArrayList() // folders that were successfully deleted
        for(folder in folders){

            val cacheResult = safeCacheCall(IO){
                folderCacheDataSource.deleteFolder(folder.folder_id)
            }

            val response = object: CacheResponseHandler<FolderListViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: Int): DataState<FolderListViewState>? {
                    if(resultObj < 0){ // if error
                        onDeleteError = true
                    }
                    else{
                        successfulDeletes.add(folder)

                        // if folder delete success then move notes to notes folder
                        safeApiCall(IO) {
                            noteCacheDataSource.deleteNotesByFolderId(
                                folder.folder_id
                            )
                        }
                    }
                    return null
                }
            }.getResult()

            // check for random errors
            if(response?.stateMessage?.response?.message
                    ?.contains(stateEvent.errorInfo()) == true){
                onDeleteError = true
            }

        }

        if(onDeleteError){
            emit(
                DataState.data<FolderListViewState>(
                    response = Response(
                        message = DELETE_FOLDERS_ERRORS,
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        }
        else{
            Log.d("cachedNotesList", "cachedNotesList: $cachedNotesList")

            emit(
                DataState.data<FolderListViewState>(
                    response = Response(
                        message = DELETE_FOLDERS_SUCCESS,
                        uiComponentType = UIComponentType.Toast(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        }

        updateNetwork(successfulDeletes, cachedNotesList)
    }

    private suspend fun updateNetwork(
        successfulDeletes: ArrayList<Folder>,
        cachedNotesList: List<Note>
    ){
        safeApiCall(IO){
            noteNetworkDataSource.moveNotes(cachedNotesList, DEFAULT_FOLDER_ID)
        }

        for (folder in successfulDeletes){

            // delete from "folders" node
            safeApiCall(IO){
                folderNetworkDataSource.deleteFolder(folder.folder_id)
            }

            // insert into "deletes" node
            safeApiCall(IO){
                folderNetworkDataSource.insertDeletedFolder(folder)
            }
        }
    }

    private suspend fun getCachedNotesByFolderId(folders: List<Folder>): List<Note> {
        val cacheResult = safeCacheCall(IO){
            noteCacheDataSource.getAllNotesByNoteFolderIds(folders)
        }

        val response = object: CacheResponseHandler<List<Note>, List<Note>>(
            response = cacheResult,
            stateEvent = null
        ){
            override suspend fun handleSuccess(resultObj: List<Note>): DataState<List<Note>>? {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }

        }.getResult()

        return response?.data ?: ArrayList()
    }

    companion object{
        val DELETE_FOLDERS_SUCCESS = "Successfully deleted folders."
        val DELETE_FOLDERS_ERRORS = "Not all the folders you selected were deleted. There was some errors."
        val DELETE_FOLDERS_YOU_MUST_SELECT = "You haven't selected any folders to delete."
        val DELETE_FOLDERS_ARE_YOU_SURE = "Are you sure you want to delete these?"
    }
}













