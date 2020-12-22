package cho.chonotes.framework.presentation.common

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cho.chonotes.business.domain.model.FolderFactory
import cho.chonotes.business.domain.model.NoteFactory
import cho.chonotes.business.interactors.folderlist.FolderListInteractors
import cho.chonotes.business.interactors.notedetail.NoteDetailInteractors
import cho.chonotes.business.interactors.notelist.NoteListInteractors
import cho.chonotes.framework.presentation.folderlist.FolderListViewModel
import cho.chonotes.framework.presentation.notedetail.NoteDetailViewModel
import cho.chonotes.framework.presentation.notelist.NoteListViewModel
import cho.chonotes.framework.presentation.splash.NoteNetworkSyncManager
import cho.chonotes.framework.presentation.splash.SplashViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
class NoteViewModelFactory
@Inject
constructor(
    private val folderListInteractors: FolderListInteractors,
    private val folderFactory: FolderFactory,
    private val noteListInteractors: NoteListInteractors,
    private val noteDetailInteractors: NoteDetailInteractors,
    private val noteNetworkSyncManager: NoteNetworkSyncManager,
    private val noteFactory: NoteFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when(modelClass){

            FolderListViewModel::class.java -> {
                FolderListViewModel(
                    folderInteractors = folderListInteractors,
                    folderFactory = folderFactory,
                    editor = editor,
                    sharedPreferences = sharedPreferences
                ) as T
            }

            NoteListViewModel::class.java -> {
                NoteListViewModel(
                    noteInteractors = noteListInteractors,
                    noteFactory = noteFactory,
                    editor = editor,
                    sharedPreferences = sharedPreferences
                ) as T
            }

            NoteDetailViewModel::class.java -> {
                NoteDetailViewModel(
                    noteInteractors = noteDetailInteractors
                ) as T
            }

            SplashViewModel::class.java -> {
                SplashViewModel(
                    noteNetworkSyncManager = noteNetworkSyncManager
                ) as T
            }

            else -> {
                throw IllegalArgumentException("unknown model class $modelClass")
            }
        }
    }
}




















