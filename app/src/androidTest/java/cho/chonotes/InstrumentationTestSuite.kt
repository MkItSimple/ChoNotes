package cho.chonotes

import cho.chonotes.framework.datasource.cache.NoteDaoServiceTests
import cho.chonotes.framework.datasource.network.NoteFirestoreServiceTests
import cho.chonotes.framework.presentation.end_to_end.NotesFeatureTest
import cho.chonotes.framework.presentation.notedetail.NoteDetailFragmentTests
import cho.chonotes.framework.presentation.notelist.NoteListFragmentTests
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite


@FlowPreview
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@RunWith(Suite::class)
@Suite.SuiteClasses(
    NoteDaoServiceTests::class,
    NoteFirestoreServiceTests::class,
    NoteDetailFragmentTests::class,
    NoteListFragmentTests::class,
    NotesFeatureTest::class
)
class InstrumentationTestSuite

























