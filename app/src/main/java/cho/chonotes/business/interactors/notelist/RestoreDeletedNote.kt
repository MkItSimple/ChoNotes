package cho.chonotes.business.interactors.notelist

import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.data.util.safeApiCall
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.framework.presentation.notelist.state.NoteListViewState
import cho.chonotes.framework.presentation.notelist.state.NoteListViewState.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RestoreDeletedNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){

    fun restoreDeletedNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val cacheResult = safeCacheCall(IO){
            noteCacheDataSource.insertNote(note)
        }

        val response = object: CacheResponseHandler<NoteListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Long): DataState<NoteListViewState>? {
                return if(resultObj > 0){
                    val viewState =
                        NoteListViewState(
                            notePendingDelete = NotePendingDelete(
                                note = note
                            )
                        )
                    DataState.data(
                        response = Response(
                            message = RESTORE_NOTE_SUCCESS,
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
                            message = RESTORE_NOTE_FAILED,
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

        updateNetwork(response?.stateMessage?.response?.message, note)
    }

    private suspend fun updateNetwork(response: String?, note: Note) {
        if(response.equals(RESTORE_NOTE_SUCCESS)){

            safeApiCall(IO){
                noteNetworkDataSource.insertOrUpdateNote(note)
            }

            safeApiCall(IO){
                noteNetworkDataSource.deleteDeletedNote(note)
            }
        }
    }

    companion object{

        const val RESTORE_NOTE_SUCCESS = "Successfully restored the deleted note."
        const val RESTORE_NOTE_FAILED = "Failed to restore the deleted note."

    }
}













