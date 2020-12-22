package cho.chonotes.business.data.cache

import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.framework.datasource.cache.database.NOTE_PAGINATION_PAGE_SIZE
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

const val FORCE_DELETE_NOTE_EXCEPTION = "FORCE_DELETE_NOTE_EXCEPTION"
const val FORCE_DELETES_NOTE_EXCEPTION = "FORCE_DELETES_NOTE_EXCEPTION"
const val FORCE_UPDATE_NOTE_EXCEPTION = "FORCE_UPDATE_NOTE_EXCEPTION"
const val FORCE_NEW_NOTE_EXCEPTION = "FORCE_NEW_NOTE_EXCEPTION"
const val FORCE_SEARCH_NOTES_EXCEPTION = "FORCE_SEARCH_NOTES_EXCEPTION"
const val FORCE_GENERAL_FAILURE = "FORCE_GENERAL_FAILURE"

class FakeNoteCacheDataSourceImpl
constructor(
    private val notesData: HashMap<String, Note>,
    private val dateUtil: DateUtil
): NoteCacheDataSource{

    override suspend fun insertNote(note: Note): Long {
        if(note.note_id == FORCE_NEW_NOTE_EXCEPTION){
            throw Exception("Something went wrong inserting the note.")
        }
        if(note.note_id == FORCE_GENERAL_FAILURE){
            return -1 // fail
        }
        notesData[note.note_id] = note
        return 1 // success
    }

    override suspend fun deleteNote(primaryKey: String): Int {
        if(primaryKey == FORCE_DELETE_NOTE_EXCEPTION){
            throw Exception("Something went wrong deleting the note.")
        }
        else if(primaryKey == FORCE_DELETES_NOTE_EXCEPTION){
            throw Exception("Something went wrong deleting the note.")
        }
        return notesData.remove(primaryKey)?.let {
            1 // return 1 for success
        }?: - 1 // -1 for failure
    }

    override suspend fun deleteNotes(notes: List<Note>): Int {
        var failOrSuccess = 1
        for(note in notes){
            if(notesData.remove(note.note_id) == null){
                failOrSuccess = -1 // mark for failure
            }
        }
        return failOrSuccess
    }

    override suspend fun deleteNotesByFolderId(folderId: String): Int {
        var failOrSuccess = 1

        if(notesData.remove(folderId) == null){
            failOrSuccess = -1 // mark for failure
        }

        return failOrSuccess
    }

    override suspend fun updateNote(
        primaryKey: String,
        newTitle: String,
        newBody: String?,
        newFolderId: String?,
        timestamp: String?
    ): Int {
        if(primaryKey == FORCE_UPDATE_NOTE_EXCEPTION){
            throw Exception("Something went wrong updating the note.")
        }
        val updatedNote = Note(
            note_id = primaryKey,
            title = newTitle,
            body = newBody?: "",
            note_folder_id = newFolderId?: "",
            uid = notesData[primaryKey]?.uid?: "",
            updated_at = timestamp?: dateUtil.getCurrentTimestamp(),
            created_at = notesData[primaryKey]?.created_at?: dateUtil.getCurrentTimestamp()
        )
        return notesData[primaryKey]?.let {
            notesData[primaryKey] = updatedNote
            1 // success
        }?: -1 // nothing to update
    }

    override suspend fun moveNotes(
        notes: List<Note>,
        newFolderId: String?,
        timestamp: String?
    ): Int {
        var failOrSuccess = 1
        for(note in notes){
            val updatedNote = Note(
                note_id = note.note_id,
                title = note.title,
                body = note.body,
                note_folder_id = newFolderId?: "",
                uid = note.uid,
                updated_at = timestamp?: dateUtil.getCurrentTimestamp(),
                created_at = notesData[note.note_id]?.created_at?: dateUtil.getCurrentTimestamp()
            )

            failOrSuccess =  notesData[note.note_id]?.let {
                notesData[note.note_id] = updatedNote
                1 // success
            }?: -1 // nothing to update
        }

        return failOrSuccess
    }

    override suspend fun moveNotesToDefaultFolder(
        folderId: String?,
        defaultFolderId: String,
        timestamp: String?
    ): Int {
        TODO("Not yet implemented")
    }

    override suspend fun moveNotesBackToRestore(
        folderId: String?,
        defaultFolderId: String,
        timestamp: String?
    ): Int {
        TODO("Not yet implemented")
    }

    // Not testing the order/filter. Just basic query
    // simulate SQLite "LIKE" query on title and body
    override suspend fun searchNotes(
        uid: String,
        query: String,
        folderId: String,
        filterAndOrder: String,
        page: Int
    ): List<Note> {
        if(query == FORCE_SEARCH_NOTES_EXCEPTION){
            throw Exception("Something went searching the cache for notes.")
        }
        val results: ArrayList<Note> = ArrayList()
        for(note in notesData.values){
            if(note.title.contains(query)){
                results.add(note)
            }
            else if(note.body.contains(query)){
                results.add(note)
            }
            if(results.size > (page * NOTE_PAGINATION_PAGE_SIZE)){
                break
            }
        }
        return results
    }

    override suspend fun getAllNotes(currentUserID: String): List<Note> {
        return ArrayList(notesData.values)
    }

    override suspend fun getAllNotesByFolderId(folderId: String): List<Note> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllNotesByNoteFolderIds(folders: List<Folder>): List<Note> {
        TODO("Not yet implemented")
    }

    override suspend fun searchNoteById(id: String): Note? {
        return notesData[id]
    }

    override suspend fun getNumNotes(): Int {
        return notesData.size
    }

    override suspend fun insertNotes(notes: List<Note>): LongArray {
        val results = LongArray(notes.size)
        for((index,note) in notes.withIndex()){
            results[index] = 1
            notesData[note.note_id] = note
        }
        return results
    }
}















