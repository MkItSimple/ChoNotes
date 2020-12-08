package cho.chonotes.di

import cho.chonotes.framework.presentation.BaseApplication
import cho.chonotes.framework.presentation.MainActivity
import cho.chonotes.framework.presentation.auth.AuthActivity
import cho.chonotes.framework.presentation.folderlist.FolderListFragment
import cho.chonotes.framework.presentation.splash.NoteNetworkSyncManager
import cho.chonotes.framework.presentation.notedetail.NoteDetailFragment
import cho.chonotes.framework.presentation.notelist.NoteListFragment
import cho.chonotes.framework.presentation.selectfolder.SelectFolderFragment
import cho.chonotes.framework.presentation.splash.SplashFragment
import cho.chonotes.notes.di.NoteViewModelModule
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Singleton
@Component(
    modules = [
        ProductionModule::class,
        AppModule::class,
        NoteViewModelModule::class,
        NoteFragmentFactoryModule::class
    ]
)
interface AppComponent {

    val noteNetworkSync: NoteNetworkSyncManager

    @Component.Factory
    interface Factory{

        fun create(@BindsInstance app: BaseApplication): AppComponent
    }

    fun inject(authActivity: AuthActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(splashFragment: SplashFragment)

    fun inject(noteListFragment: NoteListFragment)

    fun inject(noteDetailFragment: NoteDetailFragment)

    fun inject(folderListFragment: FolderListFragment)

    fun inject(selectFolderFragment: SelectFolderFragment)
}












