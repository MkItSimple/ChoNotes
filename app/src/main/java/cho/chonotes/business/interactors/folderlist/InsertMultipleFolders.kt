package cho.chonotes.business.interactors.folderlist

import cho.chonotes.business.data.cache.abstraction.FolderCacheDataSource
import cho.chonotes.business.data.network.abstraction.FolderNetworkDataSource
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.business.data.util.safeApiCall
import cho.chonotes.business.data.util.safeCacheCall
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// For testing
class InsertMultipleFolders(
    private val folderCacheDataSource: FolderCacheDataSource,
    private val folderNetworkDataSource: FolderNetworkDataSource
){

    fun insertFolders(
        numFolders: Int,
        stateEvent: StateEvent
    ): Flow<DataState<FolderListViewState>?> = flow {

        val folderList = FolderListTester.generateFolderList(numFolders)
        safeCacheCall(IO){
            folderCacheDataSource.insertFolders(folderList)
        }

        emit(
            DataState.data<FolderListViewState>(
                response = Response(
                    message = "success",
                    uiComponentType = UIComponentType.None(),
                    messageType = MessageType.None()
                ),
                data = null,
                stateEvent = stateEvent
            )
        )

        updateNetwork(folderList)
    }

    private suspend fun updateNetwork(folderList: List<Folder>){
        safeApiCall(IO){
            folderNetworkDataSource.insertOrUpdateFolders(folderList)
        }
    }

}


private object FolderListTester {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val dateUtil =
        DateUtil(dateFormat)

    fun generateFolderList(numFolders: Int): List<Folder>{
        val list: ArrayList<Folder> = ArrayList()
        for(id in 0..numFolders){
            list.add(generateFolder())
        }
        return list
    }

    fun generateFolder(): Folder {
        val folder = Folder(
            folder_id = UUID.randomUUID().toString(),
            folder_name = UUID.randomUUID().toString(),
            notes_count = 0,
            uid = UUID.randomUUID().toString(),
            created_at = dateUtil.getCurrentTimestamp(),
            updated_at = dateUtil.getCurrentTimestamp()
        )
        return folder
    }
}