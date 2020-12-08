package cho.chonotes.business.interactors.common

import android.util.Log
import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.FolderCacheDataSource
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.network.ApiResponseHandler
import cho.chonotes.business.data.network.abstraction.FolderNetworkDataSource
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.data.util.safeApiCall
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.business.domain.model.Note
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteFolder<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val folderCacheDataSource: FolderCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val folderNetworkDataSource: FolderNetworkDataSource
){

    fun deleteFolder(
        folder: Folder,
        stateEvent: StateEvent
    ): Flow<DataState<ViewState>?> = flow {
        val cachedNotesList = getCachedNotesByFolderId(folder.folder_id)
//        val networkNotesList = getNetworkNotes()

        val cacheResult = safeCacheCall(IO){
            folderCacheDataSource.deleteFolder(folder.folder_id)
        }

        val response = object: CacheResponseHandler<ViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Int): DataState<ViewState>? {
                return if(resultObj > 0){

                    // if folder delete success then move notes to notes folder
                    safeApiCall(IO) {
                        noteCacheDataSource.moveNotesToDefaultFolder(
                            folder.folder_id,
                            DEFAULT_FOLDER_ID,
                            null
                        )
                    }

                    DataState.data(
                        response = Response(
                            message = DELETE_FOLDER_SUCCESS,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
                else{
                    DataState.data(
                        response = Response(
                            message = DELETE_FOLDER_FAILED,
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

        // update network
        if(response?.stateMessage?.response?.message.equals(DELETE_FOLDER_SUCCESS)){
            Log.d("cachedNotesList", "cachedNotesList $cachedNotesList")

            // move notes to default folder
            safeApiCall(IO){
                noteNetworkDataSource.moveNotes(cachedNotesList, DEFAULT_FOLDER_ID)
            }

            // delete from 'folders' node
            safeApiCall(IO){
                folderNetworkDataSource.deleteFolder(folder.folder_id)
            }

            // insert into 'deletes' node
            safeApiCall(IO){
                folderNetworkDataSource.insertDeletedFolder(folder)
            }

        }
    }

    private suspend fun getCachedNotesByFolderId(folderId: String): List<Note> {
        val cacheResult = safeCacheCall(IO){
            noteCacheDataSource.getAllNotesByFolderId(folderId)
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

    private suspend fun getNetworkNotes(): List<Note>{
        val networkResult = safeApiCall(IO){
            noteNetworkDataSource.getAllNotes()
        }

        val response = object: ApiResponseHandler<List<Note>, List<Note>>(
            response = networkResult,
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
        val DELETE_FOLDER_SUCCESS = "Successfully deleted folder."
        val DELETE_FOLDER_PENDING = "Delete pending..."
        val DELETE_FOLDER_FAILED = "Failed to delete folder."
        val DELETE_ARE_YOU_SURE = "Are you sure you want to delete this?"

        val DEFAULT_FOLDER_ID = "notes"
    }
}













