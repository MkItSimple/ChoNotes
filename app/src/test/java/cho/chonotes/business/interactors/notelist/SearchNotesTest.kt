package cho.chonotes.business.interactors.notelist

import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.domain.state.DataState
import cho.chonotes.business.data.cache.CacheErrors
import cho.chonotes.business.data.cache.FORCE_SEARCH_NOTES_EXCEPTION
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.model.NoteFactory
import cho.chonotes.business.interactors.notelist.SearchNotes.Companion.SEARCH_NOTES_NO_MATCHING_RESULTS
import cho.chonotes.business.interactors.notelist.SearchNotes.Companion.SEARCH_NOTES_SUCCESS
import cho.chonotes.di.DependencyContainer
import cho.chonotes.framework.datasource.cache.database.ORDER_BY_ASC_DATE_UPDATED
import cho.chonotes.framework.presentation.notelist.state.NoteListStateEvent
import cho.chonotes.framework.presentation.notelist.state.NoteListStateEvent.*
import cho.chonotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.ArrayList

/*

Test cases:
1. blankQuery_success_confirmNotesRetrieved()
    a) query with some default search options
    b) listen for SEARCH_NOTES_SUCCESS emitted from flow
    c) confirm notes were retrieved
    d) confirm notes in cache match with notes that were retrieved
2. randomQuery_success_confirmNoResults()
    a) query with something that will yield no results
    b) listen for SEARCH_NOTES_NO_MATCHING_RESULTS emitted from flow
    c) confirm nothing was retrieved
    d) confirm there is notes in the cache
3. searchNotes_fail_confirmNoResults()
    a) force an exception to be thrown
    b) listen for CACHE_ERROR_UNKNOWN emitted from flow
    c) confirm nothing was retrieved
    d) confirm there is notes in the cache


 */
@InternalCoroutinesApi
class SearchNotesTest {

    // system in test
    private val searchNotes: SearchNotes

    private val insertNewNote: InsertNewNote // added

    // dependencies
    private val dependencyContainer: DependencyContainer = DependencyContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteFactory: NoteFactory

    private val noteNetworkDataSource: NoteNetworkDataSource // added

    init {
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteFactory = dependencyContainer.noteFactory

        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource // added

        searchNotes = SearchNotes(
            noteCacheDataSource = noteCacheDataSource
        )

        insertNewNote = InsertNewNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource,
            noteFactory = noteFactory
        )
    }

    @Test
    fun blankQuery_success_confirm() = runBlocking {
    //fun blankQuery_success_confirmNotesRetrieved() = runBlocking {

        val query = ""
        var results: ArrayList<Note>? = null
        searchNotes.searchNotes(
            uid = "",
            query = query,
            folderId = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = SearchNotesEvent()
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    SEARCH_NOTES_SUCCESS
                )
                value?.data?.noteList?.let { list ->
                    results = ArrayList(list)
                }

            }
        })

        // confirm notes were retrieved
        assertTrue { results != null }

        // confirm notes in cache match with notes that were retrieved
        val notesInCache = noteCacheDataSource.searchNotes(
            uid = "",
            query = query,
            folderId = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue { results?.containsAll(notesInCache)?:false }
    }

    @Test
    fun randomQuery_success_confirmNoResults() = runBlocking {

        val query = "hthrthrgrkgenrogn843nn4u34n934v53454hrth"
        var results: ArrayList<Note>? = null
        searchNotes.searchNotes(
            uid = "",
            query = query,
            folderId = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = SearchNotesEvent()
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    SEARCH_NOTES_NO_MATCHING_RESULTS
                )
                value?.data?.noteList?.let { list ->
                    results = ArrayList(list)
                }
            }
        })

//        // confirm nothing was retrieved
        assertTrue { results?.run { size == 0 }?: true }
//        results?.let{
//            assertTrue { it.size == 0 }
//        }


        // confirm there is notes in the cache
        val notesInCache = noteCacheDataSource.searchNotes(
            uid = "",
            query = "",
            folderId = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )

//        notesInCache?.let {
            assertTrue{ notesInCache.isNotEmpty() }
//        }

    }

    @Test
    fun searchNotes_fail_confirmNoResults() = runBlocking {

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
            stateEvent = InsertNewNoteEvent(
                newNote.title,
                newNote.note_folder_id
            )
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                // Success fully inserted dummy note
            }
        })

        val query = FORCE_SEARCH_NOTES_EXCEPTION
        var results: ArrayList<Note>? = null
        searchNotes.searchNotes(
            uid = newNote.uid,
            query = query,
            folderId = newNote.note_folder_id,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = SearchNotesEvent()
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
                )
                value?.data?.noteList?.let { list ->
                    results = ArrayList(list)
                }
                println("results: ${results}")
            }
        })

        // confirm nothing was retrieved
        assertTrue { results?.run { size == 0 }?: true }

        // confirm there is notes in the cache
        val notesInCache = noteCacheDataSource.searchNotes(
            uid = "",
            query = "",
            folderId = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue { notesInCache.isNotEmpty() }
    }


}
















