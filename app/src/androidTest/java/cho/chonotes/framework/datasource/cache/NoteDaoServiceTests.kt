package cho.chonotes.framework.datasource.cache

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import cho.chonotes.BaseTest
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.model.NoteFactory
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.di.TestAppComponent
import cho.chonotes.framework.datasource.cache.abstraction.NoteDaoService
import cho.chonotes.framework.datasource.cache.database.NoteDao
import cho.chonotes.framework.datasource.cache.implementation.NoteDaoServiceImpl
import cho.chonotes.framework.datasource.cache.mappers.CacheMapper
import cho.chonotes.framework.datasource.data.NoteDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


// runBlockingTest doesn't work:
// https://github.com/Kotlin/kotlinx.coroutines/issues/1204

/*
    LEGEND:
    1. CBS = "Confirm by searching"

    Test cases:
    1. confirm database note empty to start (should be test data inserted from CacheTest.kt)
    2. insert a new note, CBS
    3. insert a list of notes, CBS
    4. insert 1000 new notes, confirm filtered search query works correctly
    5. insert 1000 new notes, confirm db size increased
    6. delete new note, confirm deleted
    7. delete list of notes, CBS
    8. update a note, confirm updated
    9. search notes, order by date (ASC), confirm order
    10. search notes, order by date (DESC), confirm order
    11. search notes, order by title (ASC), confirm order
    12. search notes, order by title (DESC), confirm order

 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class NoteDaoServiceTests: BaseTest(){


    // system in test
    private val noteDaoService: NoteDaoService

    // dependencies
    @Inject
    lateinit var dao: NoteDao

    @Inject
    lateinit var noteDataFactory: NoteDataFactory

    @Inject
    lateinit var dateUtil: DateUtil

    @Inject
    lateinit var noteFactory: NoteFactory

    @Inject
    lateinit var cacheMapper: CacheMapper

    init {
        injectTest()
        insertTestData()
        noteDaoService = NoteDaoServiceImpl(
            noteDao = dao,
            noteMapper = cacheMapper,
            dateUtil = dateUtil
        )
    }

    override fun injectTest() {
        (application.appComponent as TestAppComponent)
            .inject(this)
    }

    private fun insertTestData() = runBlocking{
        val entityList = cacheMapper.noteListToEntityList(
            noteDataFactory.produceListOfNotes()
        )
        dao.insertNotes(entityList)
    }

    /**
     * This test runs first. Check to make sure the test data was inserted from
     * CacheTest class.
     */
    @Test
    fun a_searchNotes_confirmDbNotEmpty() = runBlocking {

        val numNotes = noteDaoService.getNumNotes()

        assertTrue { numNotes > 0 }

    }

    @Test
    fun insertNote_CBS() = runBlocking {

        val newNote = noteFactory.createSingleNote(
            null,
            "Super cool title",
            "Some content for the note"
            )
        noteDaoService.insertNote(newNote)

        val notes = noteDaoService.searchNotes()
        assert(notes.contains(newNote))
    }

    @Test
    fun insertNoteList_CBS() = runBlocking {

        val noteList = noteFactory.createNoteList(10)
        noteDaoService.insertNotes(noteList)

        val queriedNotes = noteDaoService.searchNotes()

        assertTrue { queriedNotes.containsAll(noteList) }
    }

    @Test
    fun insert1000Notes_confirmNumNotesInDb() = runBlocking {
        val currentNumNotes = noteDaoService.getNumNotes()

        // insert 1000 notes
        val noteList = noteFactory.createNoteList(1000)
        noteDaoService.insertNotes(noteList)

        val numNotes = noteDaoService.getNumNotes()
        assertEquals(currentNumNotes + 1000, numNotes)
    }

    @Test
    fun insert1000Notes_searchNotesByTitle_confirm50ExpectedValues() = runBlocking {

        // insert 1000 notes
        val noteList = noteFactory.createNoteList(1000)
        noteDaoService.insertNotes(noteList)

        // query 50 notes by specific title
        repeat(50){
            val randomIndex = Random.nextInt(0, noteList.size - 1)
            val result = noteDaoService.searchNotesOrderByTitleASC(
                uid = noteList[randomIndex].uid,
                query = noteList[randomIndex].title,
                folderId = noteList[randomIndex].note_folder_id,
                page = 1,
                pageSize = 1
            )
            assertEquals(noteList[randomIndex].title, result[0].title)
        }
    }



    @Test
    fun insertNote_deleteNote_confirmDeleted() = runBlocking {
        val newNote = noteFactory.createSingleNote(
            null,
            "Super cool title",
            "Some content for the note"
        )
        noteDaoService.insertNote(newNote)

        var notes = noteDaoService.searchNotes()
        assert(notes.contains(newNote))

        noteDaoService.deleteNote(newNote.note_id)
        notes = noteDaoService.searchNotes()
        assert(!notes.contains(newNote))
    }

    @Test
    fun deleteNoteList_confirmDeleted() = runBlocking {
        val noteList: ArrayList<Note> = ArrayList(noteDaoService.searchNotes())

        // select some random notes for deleting
        val notesToDelete: ArrayList<Note> = ArrayList()

        // 1st
        var noteToDelete = noteList[Random.nextInt(0, noteList.size - 1) + 1]
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 2nd
        noteToDelete = noteList[Random.nextInt(0, noteList.size - 1) + 1]
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 3rd
        noteToDelete = noteList[Random.nextInt(0, noteList.size - 1) + 1]
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        // 4th
        noteToDelete = noteList[Random.nextInt(0, noteList.size - 1) + 1]
        noteList.remove(noteToDelete)
        notesToDelete.add(noteToDelete)

        noteDaoService.deleteNotes(notesToDelete)

        // confirm they were deleted
        val searchResults = noteDaoService.searchNotes()
        assertFalse { searchResults.containsAll(notesToDelete) }
    }

    @Test
    fun insertNote_updateNote_confirmUpdated() = runBlocking {
        val newNote = noteFactory.createSingleNote(
            null,
            "Super cool title",
            "Some content for the note",
            "folder id"
        )
        noteDaoService.insertNote(newNote)

        val newTitle = UUID.randomUUID().toString()
        val newBody = UUID.randomUUID().toString()
        noteDaoService.updateNote(
            primaryKey = newNote.note_id,
            title = newTitle,
            body = newBody,
            folder_id = newNote.note_folder_id,
            timestamp = null
        )

        val notes = noteDaoService.searchNotes()

        var foundNote = false
        for(note in notes){
            if(note.note_id == newNote.note_id){
                foundNote = true
                assertEquals(newNote.note_id, note.note_id)
                assertEquals(newTitle, note.title)
                assertEquals(newBody, note.body)
                assert(newNote.updated_at != note.updated_at)
                assertEquals(
                    newNote.created_at,
                    note.created_at
                )
                break
            }
        }
        assertTrue { foundNote }
    }

    @Test
    fun searchNotes_orderByDateASC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByDateASC(
            uid = "",
            query = "",
            folderId = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previousNoteDate = noteList[0].updated_at
        for(index in 1 until noteList.size){
            val currentNoteDate = noteList[index].updated_at
            assertTrue { currentNoteDate >= previousNoteDate }
            previousNoteDate = currentNoteDate
        }
    }


    @Test
    fun searchNotes_orderByDateDESC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByDateDESC(
            uid = "",
            query = "",
            folderId = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = noteList[0].updated_at
        for(index in 1 until noteList.size){
            val current = noteList[index].updated_at
            assertTrue { current <= previous }
            previous = current
        }
    }

    @Test
    fun searchNotes_orderByTitleASC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByTitleASC(
            uid = "",
            query = "",
            folderId = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = noteList[0].title
        for(index in 1 until noteList.size){
            val current = noteList[index].title

            assertTrue {
                listOf(previous, current)
                    .asSequence()
                    .zipWithNext { a, b ->
                        a <= b
                    }.all { it }
            }
            previous = current
        }
    }

    @Test
    fun searchNotes_orderByTitleDESC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByTitleDESC(
            uid = "",
            query = "",
            folderId = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = noteList[0].title
        for(index in 1 until noteList.size){
            val current = noteList[index].title

            assertTrue {
                listOf(previous, current)
                    .asSequence()
                    .zipWithNext { a, b ->
                        a >= b
                    }.all { it }
            }
            previous = current
        }
    }
}














