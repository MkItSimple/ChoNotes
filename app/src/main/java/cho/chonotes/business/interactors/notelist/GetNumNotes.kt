package cho.chonotes.business.interactors.notelist

import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetNumNotes(
    private val noteCacheDataSource: NoteCacheDataSource
){

    fun getNumNotes(
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val cacheResult = safeCacheCall(IO){
            noteCacheDataSource.getNumNotes()
        }
        val response =  object: CacheResponseHandler<NoteListViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ){
            override suspend fun handleSuccess(resultObj: Int): DataState<NoteListViewState>? {
                val viewState = NoteListViewState(
                    numNotesInCache = resultObj
                )
                return DataState.data(
                    response = Response(
                        message = GET_NUM_NOTES_SUCCESS,
                        uiComponentType = UIComponentType.None(),
                        messageType = MessageType.Success()
                    ),
                    data = viewState,
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(response)
    }

    companion object{
        const val GET_NUM_NOTES_SUCCESS = "Successfully retrieved the number of notes from the cache."
        const val GET_NUM_NOTES_FAIL = "Failed to retrieve the number of notes from the cache."
    }
}