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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

@Suppress("IMPLICIT_CAST_TO_ANY")
class SyncNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){
//    private val currentUserID = FirebaseAuth.getInstance().currentUser?.let {
//        it.uid
//    } ?: "GIOqxtta3zdTPmlvLmZLTiyCfGp2"

    suspend fun syncNotes() {

        val cachedNotesList = getCachedNotes()

        val networkNotesList = getNetworkNotes()

        syncNetworkNotesWithCachedNotes(
            ArrayList(cachedNotesList),
            networkNotesList
        )
    }

    private suspend fun getCachedNotes(): List<Note> {
        val cacheResult = safeCacheCall(IO){
            noteCacheDataSource.getAllNotes("GIOqxtta3zdTPmlvLmZLTiyCfGp2")
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

    private suspend fun syncNetworkNotesWithCachedNotes(
        cachedNotes: ArrayList<Note>,
        networkNotes: List<Note>
    ) = withContext(IO){

        for(note in networkNotes){
            noteCacheDataSource.searchNoteById(note.note_id)?.let { cachedNote ->
                cachedNotes.remove(cachedNote)
                checkIfCachedNoteRequiresUpdate(cachedNote, note)
            }?: noteCacheDataSource.insertNote(note)
        }

        for(cachedNote in cachedNotes){
            noteNetworkDataSource.insertOrUpdateNote(cachedNote)
        }
    }

    private suspend fun checkIfCachedNoteRequiresUpdate(
        cachedNote: Note,
        networkNote: Note
    ){
        val cacheUpdatedAt = cachedNote.updated_at
        val networkUpdatedAt = networkNote.updated_at

        if(networkUpdatedAt > cacheUpdatedAt){
            printLogD("SyncNotes",
                "cacheUpdatedAt: ${cacheUpdatedAt}, " +
                        "networkUpdatedAt: ${networkUpdatedAt}, " +
                        "note: ${cachedNote.title}")
            safeCacheCall(IO){
                noteCacheDataSource.updateNote(
                    networkNote.note_id,
                    networkNote.title,
                    networkNote.body,
                    networkNote.note_folder_id,
                    networkNote.updated_at
                )
            }
        } else if(networkUpdatedAt < cacheUpdatedAt){
            safeApiCall(IO){
                noteNetworkDataSource.insertOrUpdateNote(cachedNote)
            }
        }
    }

}






























