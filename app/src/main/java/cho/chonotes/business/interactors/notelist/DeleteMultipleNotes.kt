package cho.chonotes.business.interactors.notelist

import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.data.util.safeApiCall
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteMultipleNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){

    private var onDeleteError: Boolean = false

    fun deleteNotes(
        notes: List<Note>,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val successfulDeletes: ArrayList<Note> = ArrayList()
        for(note in notes){
            val cacheResult = safeCacheCall(IO){
                noteCacheDataSource.deleteNote(note.note_id)
            }

            val response = object: CacheResponseHandler<NoteListViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: Int): DataState<NoteListViewState>? {
                    if(resultObj < 0){
                        onDeleteError = true
                    }
                    else{
                        successfulDeletes.add(note)
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
                DataState.data<NoteListViewState>(
                    response = Response(
                        message = DELETE_NOTES_ERRORS,
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
                DataState.data<NoteListViewState>(
                    response = Response(
                        message = DELETE_NOTES_SUCCESS,
                        uiComponentType = UIComponentType.Toast(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        }

        updateNetwork(successfulDeletes)
    }

    private suspend fun updateNetwork(successfulDeletes: ArrayList<Note>){
        for (note in successfulDeletes){

            safeApiCall(IO){
                noteNetworkDataSource.deleteNote(note)
            }

            safeApiCall(IO){
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }

    companion object{
        const val DELETE_NOTES_SUCCESS = "Successfully deleted notes."
        const val DELETE_NOTES_ERRORS = "Not all the notes you selected were deleted. There was some errors."
        const val DELETE_NOTES_YOU_MUST_SELECT = "You haven't selected any notes to delete."
        const val DELETE_NOTES_ARE_YOU_SURE = "Are you sure you want to delete these?"
    }
}













