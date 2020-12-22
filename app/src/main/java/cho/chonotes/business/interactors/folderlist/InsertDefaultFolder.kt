package cho.chonotes.business.interactors.folderlist

import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.FolderCacheDataSource
import cho.chonotes.business.data.network.abstraction.FolderNetworkDataSource
import cho.chonotes.business.data.util.safeApiCall
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.model.FolderFactory
import cho.chonotes.business.domain.state.*
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class InsertDefaultFolder(
    private val folderCacheDataSource: FolderCacheDataSource,
    private val folderNetworkDataSource: FolderNetworkDataSource,
    private val folderFactory: FolderFactory
){

    fun insertDefaultFolder(
        uid: String,
        stateEvent: StateEvent
    ): Flow<DataState<FolderListViewState>?> = flow {

        val newFolder = folderFactory.createDefaultFolder(uid)
        val cacheResult = safeCacheCall(IO){
            folderCacheDataSource.insertFolder(newFolder)
        }

        val cacheResponse = object: CacheResponseHandler<FolderListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Long): DataState<FolderListViewState>? {
                return if(resultObj > 0){
                    val viewState =
                        FolderListViewState(
                            newFolder = newFolder
                        )
                    DataState.data(
                        response = Response(
                            message = INSERT_FOLDER_SUCCESS,
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
                            message = INSERT_FOLDER_FAILED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Error()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()

        emit(cacheResponse)

        updateNetwork(cacheResponse?.stateMessage?.response?.message, newFolder)
    }

    private suspend fun updateNetwork(cacheResponse: String?, newFolder: Folder ){
        if(cacheResponse.equals(INSERT_FOLDER_SUCCESS)){

            safeApiCall(IO){
                folderNetworkDataSource.insertOrUpdateFolder(newFolder)
            }
        }
    }

    companion object{
        val INSERT_FOLDER_SUCCESS = "Successfully inserted new folder."
        val INSERT_FOLDER_FAILED = "Failed to insert new folder."
    }
}