package cho.chonotes.business.interactors.splash

import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.model.NoteFactory
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.di.DependencyContainer
import cho.chonotes.framework.datasource.cache.database.ORDER_BY_ASC_DATE_UPDATED
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.ArrayList


/*

Test cases:
1. insertNetworkNotesIntoCache()
    a) insert a bunch of new notes into the cache
    b) perform the sync
    c) check to see that those notes were inserted into the network
2. insertCachedNotesIntoNetwork()
    a) insert a bunch of new notes into the network
    b) perform the sync
    c) check to see that those notes were inserted into the cache
3. checkCacheUpdateLogicSync()
    a) select some notes from the cache and update them
    b) perform sync
    c) confirm network reflects the updates
4. checkNetworkUpdateLogicSync()
    a) select some notes from the network and update them
    b) perform sync
    c) confirm cache reflects the updates

 */

@InternalCoroutinesApi
class SyncNotesTest {

    // system in test
    private val syncNotes: SyncNotes

    // dependencies
    private val dependencyContainer: DependencyContainer = DependencyContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory
    private val dateUtil: DateUtil

    init {
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        dateUtil = dependencyContainer.dateUtil
        syncNotes = SyncNotes(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

//    @Test
//    fun doSuccessiveUpdatesOccur() = runBlocking {
//
//        // update a single note with new timestamp
//        val newDate = dateUtil.getCurrentTimestamp()
//        val updatedNote = Note(
//            note_id = noteNetworkDataSource.getAllNotes()[0].note_id,
//            title = noteNetworkDataSource.getAllNotes()[0].title,
//            body = noteNetworkDataSource.getAllNotes()[0].body,
//            note_folder_id = noteNetworkDataSource.getAllNotes()[0].note_folder_id,
//            uid = noteNetworkDataSource.getAllNotes()[0].uid,
//            created_at = noteNetworkDataSource.getAllNotes()[0].created_at,
//            updated_at = newDate
//        )
//        noteNetworkDataSource.insertOrUpdateNote(updatedNote)
//
//        syncNotes.syncNotes()
//
//        delay(2001)
//
//        // simulate launch app again
//        syncNotes.syncNotes()
//
//        // confirm the date was not updated a second time
//        val notes = noteNetworkDataSource.getAllNotes()
//        for(note in notes){
//            if(note.note_id == updatedNote.note_id){
//                assertTrue { note.updated_at == newDate }
//            }
//        }
//    }

    @Test
    fun checkUpdatedAtDates() = runBlocking {

        // update a single note with new timestamp
        val newDate = dateUtil.getCurrentTimestamp()
        val networkNotes = noteNetworkDataSource.getAllNotes()

       delay(1500)

        val updatedNote = Note(
            note_id = networkNotes[0].note_id,
            title = networkNotes[0].title,
            body = networkNotes[0].body,
            note_folder_id = networkNotes[0].note_folder_id,
            uid = networkNotes[0].uid,
            created_at = networkNotes[0].created_at,
            updated_at = newDate
        )
//        noteNetworkDataSource.insertOrUpdateNote(updatedNote)
//
//        syncNotes.syncNotes()
//
//        // confirm only a single 'updated_at' date was updated
//        val notes = noteNetworkDataSource.getAllNotes()
//        for(note in notes){
//            noteCacheDataSource.searchNoteById(note.note_id)?.let { n ->
//                println("date: ${n.updated_at}")
//                if(n.note_id == updatedNote.note_id){
//                    assertTrue { n.updated_at == newDate }
//                }
//                else{
//                    assertFalse { n.updated_at == newDate }
//                }
//            }
//        }
    }

    @Test
    fun insertNetworkNotesIntoCache() = runBlocking {

        // prepare the scenario
        // -> Notes in network are newer so they must be inserted into cache
        val newNotes = noteFactory.createNoteList(50)
        noteNetworkDataSource.insertOrUpdateNotes(newNotes)

        // perform the sync
        syncNotes.syncNotes()

        // confirm the new notes were inserted into cache
        for(note in newNotes){
            val cachedNote = noteCacheDataSource.searchNoteById(note.note_id)
            assertTrue { cachedNote != null }
        }
    }

    @Test
    fun insertCachedNotesIntoNetwork() = runBlocking {

        // prepare the scenario
        // -> Notes in cache are newer so they must be inserted into network
        val newNotes = noteFactory.createNoteList(50)
        noteCacheDataSource.insertNotes(newNotes)

        // perform the sync
        syncNotes.syncNotes()

        // confirm the new notes were inserted into network
        for(note in newNotes){
            val networkNote = noteNetworkDataSource.searchNote(note)
            assertTrue { networkNote != null }
        }
    }

    @Test
    fun checkCacheUpdateLogicSync() = runBlocking {

        // select a few notes from cache and update the title and body
        val cachedNotes = noteCacheDataSource.searchNotes(
            uid = "",
            query = "",
            folderId = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        val notesToUpdate: ArrayList<Note> = ArrayList()
        for(note in cachedNotes){
            val updatedNote = noteFactory.createSingleNote(
                note_id = note.note_id,
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString(),
                note_folder_id = UUID.randomUUID().toString(),
                uid = UUID.randomUUID().toString()
            )
            notesToUpdate.add(updatedNote)
            if(notesToUpdate.size > 3){
                break
            }
        }
        noteCacheDataSource.insertNotes(notesToUpdate)

        // perform sync
        syncNotes.syncNotes()

        // confirm the updated notes were updated in the network
        for(note in notesToUpdate){
            val networkNote = noteNetworkDataSource.searchNote(note)
            assertEquals(note.note_id, networkNote?.note_id)
            assertEquals(note.title, networkNote?.title)
            assertEquals(note.body, networkNote?.body)
            assertEquals(note.note_folder_id, networkNote?.note_folder_id)
            assertEquals(note.uid, networkNote?.uid)
            assertEquals(note.updated_at, networkNote?.updated_at)
        }
    }

    @Test
    fun checkNetworkUpdateLogicSync() = runBlocking {

        // select a few notes from network and update the title and body
        val networkNotes = noteNetworkDataSource.getAllNotes()

        val notesToUpdate: ArrayList<Note> = ArrayList()
        for(note in networkNotes){
            val updatedNote = noteFactory.createSingleNote(
                note_id = note.note_id,
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString(),
                note_folder_id = UUID.randomUUID().toString(),
                uid = UUID.randomUUID().toString()
            )
            notesToUpdate.add(updatedNote)
            if(notesToUpdate.size > 3){
                break
            }
        }
        noteNetworkDataSource.insertOrUpdateNotes(notesToUpdate)

        // perform sync
        syncNotes.syncNotes()

        // confirm the updated notes were updated in the cache
        for(note in notesToUpdate){
            val cacheNote = noteCacheDataSource.searchNoteById(note.note_id)
            assertEquals(note.note_id, cacheNote?.note_id)
            assertEquals(note.title, cacheNote?.title)
            assertEquals(note.body, cacheNote?.body)
            assertEquals(note.note_folder_id, cacheNote?.note_folder_id)
            assertEquals(note.uid, cacheNote?.uid)
            assertEquals(note.updated_at, cacheNote?.updated_at)
        }
    }
}







































