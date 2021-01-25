package cho.chonotes.business.interactors.notelist

import cho.chonotes.business.data.cache.CacheErrors
import cho.chonotes.business.data.cache.FORCE_GENERAL_FAILURE
import cho.chonotes.business.data.cache.FORCE_NEW_NOTE_EXCEPTION
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.NoteFactory
import cho.chonotes.business.interactors.notelist.InsertNewNote.Companion.INSERT_NOTE_SUCCESS
import cho.chonotes.business.domain.state.DataState
import cho.chonotes.di.DependencyContainer
import cho.chonotes.framework.presentation.notelist.state.NoteListStateEvent.*
import cho.chonotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

/*

Test cases:
1. insertNote_success_confirmNetworkAndCacheUpdated()
    a) insert a new note
    b) listen for INSERT_NOTE_SUCCESS emission from flow
    c) confirm cache was updated with new note
    d) confirm network was updated with new note
2. insertNote_fail_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force a failure (return -1 from db operation)
    c) listen for INSERT_NOTE_FAILED emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force an exception
    c) listen for CACHE_ERROR_UNKNOWN emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
 */
@InternalCoroutinesApi
class InsertNewNoteTest {

    // system in test
    private val insertNewNote: InsertNewNote

    // dependencies
    private val dependencyContainer: DependencyContainer = DependencyContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        insertNewNote = InsertNewNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource,
            noteFactory = noteFactory
        )
    }

    //fun insertNote_success_confirmNetworkAndCacheUpdated() = runBlocking {
    @Test
    fun insertN_c_s() = runBlocking {

        val newNote = noteFactory.createSingleNote(
            note_id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            note_folder_id = UUID.randomUUID().toString(),
            uid = UUID.randomUUID().toString()
        )

        insertNewNote.insertNewNote(
            note_id = newNote.note_id,
            title = newNote.title,
            toFolder = newNote.note_folder_id,
            uid = newNote.uid,
            stateEvent = InsertNewNoteEvent(newNote.title, newNote.note_folder_id)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    INSERT_NOTE_SUCCESS
                )
            }
        })

        // confirm network was updated
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertEquals(networkNoteThatWasInserted, newNote)

        // confirm cache was updated
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.note_id)
        assertEquals(cacheNoteThatWasInserted, newNote)
    }

    // insertNote_fail_confirmNetworkAndCacheUnchanged()
    @Test
    fun insertNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {

        val newNote = noteFactory.createSingleNote(
            note_id = FORCE_GENERAL_FAILURE,
            title = UUID.randomUUID().toString(),
            note_folder_id = UUID.randomUUID().toString(),
            uid = UUID.randomUUID().toString()
        )

        insertNewNote.insertNewNote(
            note_id = newNote.note_id,
            title = newNote.title,
            toFolder = newNote.note_folder_id,
            uid = newNote.uid,
            stateEvent = InsertNewNoteEvent(newNote.title, newNote.note_folder_id)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    InsertNewNote.INSERT_NOTE_FAILED
                )
            }
        })

        // confirm network was not changed
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertEquals(networkNoteThatWasInserted, null)

        // confirm cache was not changed
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.note_id)
        assertEquals(cacheNoteThatWasInserted, null)
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {

        val newNote = noteFactory.createSingleNote(
            note_id = FORCE_NEW_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            note_folder_id = UUID.randomUUID().toString(),
            uid = UUID.randomUUID().toString()
        )

        insertNewNote.insertNewNote(
            note_id = newNote.note_id,
            title = newNote.title,
            toFolder = newNote.note_folder_id,
            uid = newNote.uid,
            stateEvent = InsertNewNoteEvent(newNote.title, newNote.note_folder_id)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
                )
            }
        })

        // confirm network was not changed
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertEquals(networkNoteThatWasInserted, null)

        // confirm cache was not changed
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.note_id)
        assertTrue { cacheNoteThatWasInserted == null }
    }
}





















