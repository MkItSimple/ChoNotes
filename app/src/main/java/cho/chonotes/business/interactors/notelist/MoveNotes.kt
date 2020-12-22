package cho.chonotes.business.interactors.notelist

import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.data.util.safeApiCall
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.state.*
import cho.chonotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MoveNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){
    fun moveNotes(
        selectedNotes: List<Note>,
        folderId: String,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val cacheResult = safeCacheCall(Dispatchers.IO){
            noteCacheDataSource.moveNotes(
                notes = selectedNotes,
                newFolderId = folderId,
                timestamp = null
            )
        }

        val response = object: CacheResponseHandler<NoteListViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Int): DataState<NoteListViewState>? {
                return if(resultObj > 0){
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTES_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
                else{
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTES_FAILED,
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

        updateNetwork(selectedNotes, folderId)
    }

    private suspend fun updateNetwork(
        noteList: List<Note>,
        folderId: String
    ){
        safeApiCall(IO){
            noteNetworkDataSource.moveNotes(noteList, folderId)
        }
    }

    companion object{
        val UPDATE_NOTES_SUCCESS = "Successfully updated note."
        val UPDATE_NOTES_FAILED = "Failed to update note."
    }
}













