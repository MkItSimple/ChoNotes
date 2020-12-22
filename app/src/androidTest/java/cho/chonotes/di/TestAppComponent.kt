package cho.chonotes.di

import cho.chonotes.framework.datasource.cache.NoteDaoServiceTests
import cho.chonotes.framework.datasource.network.NoteFirestoreServiceTests
import cho.chonotes.framework.presentation.TestBaseApplication
import cho.chonotes.framework.presentation.end_to_end.NotesFeatureTest
import cho.chonotes.framework.presentation.notedetail.NoteDetailFragmentTests
import cho.chonotes.framework.presentation.notelist.NoteListFragmentTests
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
        TestModule::class,
        AppModule::class,
        TestNoteFragmentFactoryModule::class,
        NoteViewModelModule::class
    ]
)
interface TestAppComponent: AppComponent {

    @Component.Factory
    interface Factory{

        fun create(@BindsInstance app: TestBaseApplication): TestAppComponent
    }

    fun inject(noteDaoServiceTests: NoteDaoServiceTests)

    fun inject(firestoreServiceTests: NoteFirestoreServiceTests)

    fun inject(noteListFragmentTests: NoteListFragmentTests)

    fun inject(noteDetailFragmentTests: NoteDetailFragmentTests)

    fun inject(notesFeatureTest: NotesFeatureTest)
}
















