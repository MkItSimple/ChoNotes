package cho.chonotes.business.interactors.folderlist

import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.FolderCacheDataSource
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.state.*
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchFoldersWithNotes(
    private val folderCacheDataSource: FolderCacheDataSource
){

    fun searchFoldersWithNotes(
        uid: String,
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<FolderListViewState>?> = flow {
        var updatedPage = page
        if(page <= 0){
            updatedPage = 1
        }
        val cacheResult = safeCacheCall(Dispatchers.IO){
            folderCacheDataSource.searchFoldersWithNotes(
                uid = uid,
                query = query,
                filterAndOrder = filterAndOrder,
                page = updatedPage
            )
        }

        val response = object: CacheResponseHandler<FolderListViewState, List<Folder>>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: List<Folder>): DataState<FolderListViewState>? {
                var message: String? =
                    SEARCH_FOLDERS_SUCCESS
                var uiComponentType: UIComponentType? = UIComponentType.None()
                if(resultObj.size == 0){
                    message =
                        SEARCH_FOLDERS_NO_MATCHING_RESULTS
                    uiComponentType = UIComponentType.Toast()
                }
                return DataState.data(
                    response = Response(
                        message = message,
                        uiComponentType = uiComponentType as UIComponentType,
                        messageType = MessageType.Success()
                    ),
                    data = FolderListViewState(
                        folderWithNotesCacheEntityList = ArrayList(resultObj)
                    ),
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(response)
    }

    companion object{
        val SEARCH_FOLDERS_SUCCESS = "Successfully retrieved list of folders."
        val SEARCH_FOLDERS_NO_MATCHING_RESULTS = "There are no folders that match that query."
    }
}







