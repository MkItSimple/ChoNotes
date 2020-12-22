package cho.chonotes.business.interactors.folderlist

import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.FolderCacheDataSource
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.network.abstraction.FolderNetworkDataSource
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.data.util.safeApiCall
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.interactors.common.DeleteFolder.Companion.DEFAULT_FOLDER_ID
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteMultipleFolders(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val folderCacheDataSource: FolderCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val folderNetworkDataSource: FolderNetworkDataSource
){

    private var onDeleteError: Boolean = false

    fun deleteFolders(
        folders: List<Folder>,
        stateEvent: StateEvent
    ): Flow<DataState<FolderListViewState>?> = flow {
        val cachedNotesList = getCachedNotesByFolderId(folders)

        val successfulDeletes: ArrayList<Folder> = ArrayList()
        for(folder in folders){

            val cacheResult = safeCacheCall(IO){
                folderCacheDataSource.deleteFolder(folder.folder_id)
            }

            val response = object: CacheResponseHandler<FolderListViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: Int): DataState<FolderListViewState>? {
                    if(resultObj < 0){
                        onDeleteError = true
                    }
                    else{
                        successfulDeletes.add(folder)

                        safeApiCall(IO) {
                            noteCacheDataSource.moveNotesToDefaultFolder(
                                folder.folder_id,
                                DEFAULT_FOLDER_ID,
                                null
                            )
                        }
                    }
                    return null
                }
            }.getResult()

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

            safeApiCall(IO){
                folderNetworkDataSource.deleteFolder(folder.folder_id)
            }

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
        const val DELETE_FOLDERS_SUCCESS = "Successfully deleted folders."
        const val DELETE_FOLDERS_ERRORS = "Not all the folders you selected were deleted. There was some errors."
        const val DELETE_FOLDERS_YOU_MUST_SELECT = "You haven't selected any folders to delete."
        const val DELETE_FOLDERS_ARE_YOU_SURE = "Are you sure you want to delete these?"
    }
}













