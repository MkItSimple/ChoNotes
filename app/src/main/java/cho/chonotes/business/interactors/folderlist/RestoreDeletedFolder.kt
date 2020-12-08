package cho.chonotes.business.interactors.folderlist

import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.FolderCacheDataSource
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.network.abstraction.FolderNetworkDataSource
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.data.util.safeApiCall
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.business.interactors.common.DeleteFolder
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RestoreDeletedFolder(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val folderCacheDataSource: FolderCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val folderNetworkDataSource: FolderNetworkDataSource
){

    fun restoreDeletedFolder(
        folder: Folder,
        stateEvent: StateEvent
    ): Flow<DataState<FolderListViewState>?> = flow {

        val cacheResult = safeCacheCall(IO){
            folderCacheDataSource.insertFolder(folder)
        }

        val response = object: CacheResponseHandler<FolderListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Long): DataState<FolderListViewState>? {
                return if(resultObj > 0){
                    val viewState =
                        FolderListViewState(
                            folderPendingDelete = FolderPendingDelete(
                                folder = folder
                            )
                        )
                    DataState.data(
                        response = Response(
                            message = RESTORE_FOLDER_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = viewState,
                        stateEvent = stateEvent
                    )
                }
                else{
                    DataState.data(
                        response = Response(
                            message = RESTORE_FOLDER_FAILED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Error()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()

        emit(response)

        updateNetwork(response?.stateMessage?.response?.message, folder)
    }

    private suspend fun updateNetwork(response: String?, folder: Folder) {
        if(response.equals(RESTORE_FOLDER_SUCCESS)){

            safeApiCall(IO) {
                noteCacheDataSource.moveNotesBackToRestore(
                    folder.folder_id,
                    DeleteFolder.DEFAULT_FOLDER_ID,
                    null
                )
            }

            // insert into "folders" node
            safeApiCall(IO){
                folderNetworkDataSource.insertOrUpdateFolder(folder)
            }

            // remove from "deleted" node
            safeApiCall(IO){
                folderNetworkDataSource.deleteDeletedFolder(folder)
            }
        }
    }

    companion object{

        val RESTORE_FOLDER_SUCCESS = "Successfully restored the deleted folder."
        val RESTORE_FOLDER_FAILED = "Failed to restore the deleted folder."

    }
}













