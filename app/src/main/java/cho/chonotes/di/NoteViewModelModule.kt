package cho.chonotes.di

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import cho.chonotes.business.domain.model.FolderFactory
import cho.chonotes.business.domain.model.NoteFactory
import cho.chonotes.business.interactors.folderlist.FolderListInteractors
import cho.chonotes.business.interactors.notedetail.NoteDetailInteractors
import cho.chonotes.business.interactors.notelist.NoteListInteractors
import cho.chonotes.framework.datasource.cache.database.FolderDao
import cho.chonotes.framework.presentation.common.NoteViewModelFactory
import cho.chonotes.framework.presentation.splash.NoteNetworkSyncManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object NoteViewModelModule {

    @Singleton
    @JvmStatic
    @Provides
    fun provideNoteViewModelFactory(
        folderListInteractors: FolderListInteractors,
        folderFactory: FolderFactory,
        noteListInteractors: NoteListInteractors,
        noteDetailInteractors: NoteDetailInteractors,
        noteNetworkSyncManager: NoteNetworkSyncManager,
        noteFactory: NoteFactory,
        editor: SharedPreferences.Editor,
        sharedPreferences: SharedPreferences
    ): ViewModelProvider.Factory{
        return NoteViewModelFactory(
            folderListInteractors = folderListInteractors,
            folderFactory = folderFactory,
            noteListInteractors = noteListInteractors,
            noteDetailInteractors = noteDetailInteractors,
            noteNetworkSyncManager = noteNetworkSyncManager,
            noteFactory = noteFactory,
            editor = editor,
            sharedPreferences = sharedPreferences
        )
    }

}

















