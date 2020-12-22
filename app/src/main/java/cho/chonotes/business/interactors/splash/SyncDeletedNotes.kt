package cho.chonotes.business.interactors.splash

import cho.chonotes.business.data.cache.CacheResponseHandler
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.network.ApiResponseHandler
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.data.util.safeApiCall
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.state.DataState
import cho.chonotes.util.printLogD
import kotlinx.coroutines.Dispatchers.IO

class SyncDeletedNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){

    suspend fun syncDeletedNotes(){

        val apiResult = safeApiCall(IO){
            noteNetworkDataSource.getDeletedNotes()
        }
        val response = object: ApiResponseHandler<List<Note>, List<Note>>(
            response = apiResult,
            stateEvent = null
        ){
            override suspend fun handleSuccess(resultObj: List<Note>): DataState<List<Note>>? {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }

        val notes = response.getResult()?.data?: ArrayList()

        val cacheResult = safeCacheCall(IO){
            noteCacheDataSource.deleteNotes(notes)
        }

        object: CacheResponseHandler<Int, Int>(
            response = cacheResult,
            stateEvent = null
        ){
            override suspend fun handleSuccess(resultObj: Int): DataState<Int>? {
                printLogD("SyncNotes",
                    "num deleted notes: ${resultObj}")
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }.getResult()

    }

}
























