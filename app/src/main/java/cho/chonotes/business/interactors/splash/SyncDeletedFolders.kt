package cho.chonotes.business.interactors.splash

import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.FolderCacheDataSource
import cho.chonotes.business.data.network.ApiResponseHandler
import cho.chonotes.business.data.network.abstraction.FolderNetworkDataSource
import cho.chonotes.business.data.util.safeApiCall
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.state.DataState
import cho.chonotes.util.printLogD
import kotlinx.coroutines.Dispatchers.IO

class SyncDeletedFolders(
    private val folderCacheDataSource: FolderCacheDataSource,
    private val folderNetworkDataSource: FolderNetworkDataSource
){

    suspend fun syncDeletedFolders(){

        val apiResult = safeApiCall(IO){
            folderNetworkDataSource.getDeletedFolders()
        }
        val response = object: ApiResponseHandler<List<Folder>, List<Folder>>(
            response = apiResult,
            stateEvent = null
        ){
            override suspend fun handleSuccess(resultObj: List<Folder>): DataState<List<Folder>>? {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }

        val folders = response.getResult()?.data?: ArrayList()

        val cacheResult = safeCacheCall(IO){
            folderCacheDataSource.deleteFolders(folders)
        }

        object: CacheResponseHandler<Int, Int>(
            response = cacheResult,
            stateEvent = null
        ){
            override suspend fun handleSuccess(resultObj: Int): DataState<Int>? {
                printLogD("SyncFolders",
                    "num deleted folders: ${resultObj}")
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }.getResult()
    }
}
























