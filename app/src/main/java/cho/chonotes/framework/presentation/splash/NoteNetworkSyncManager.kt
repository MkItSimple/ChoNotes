package cho.chonotes.framework.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cho.chonotes.business.interactors.splash.SyncDeletedFolders
import cho.chonotes.business.interactors.splash.SyncDeletedNotes
import cho.chonotes.business.interactors.splash.SyncFolders
import cho.chonotes.business.interactors.splash.SyncNotes
import cho.chonotes.util.printLogD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteNetworkSyncManager
@Inject
constructor(
    private val syncFolders: SyncFolders,
    private val syncDeletedFolders: SyncDeletedFolders,

    private val syncNotes: SyncNotes,
    private val syncDeletedNotes: SyncDeletedNotes
){

    private val _hasSyncBeenExecuted: MutableLiveData<Boolean> = MutableLiveData(false)

    val hasSyncBeenExecuted: LiveData<Boolean>
            get() = _hasSyncBeenExecuted

    fun executeDataSync(coroutineScope: CoroutineScope){
        if(_hasSyncBeenExecuted.value!!){
            return
        }

        val syncJob = coroutineScope.launch {
            val deletesJob = launch {
                printLogD("SyncNotes",
                    "syncing deleted notes.")
                syncDeletedFolders.syncDeletedFolders()
                syncDeletedNotes.syncDeletedNotes()
            }
            deletesJob.join()

            launch {
                printLogD("SyncNotes",
                    "syncing notes.")
                syncFolders.syncFolders()
                syncNotes.syncNotes()
            }
        }
        syncJob.invokeOnCompletion {
            CoroutineScope(Main).launch{
                _hasSyncBeenExecuted.value = true
            }
        }
    }

}





















