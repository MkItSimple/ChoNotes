package cho.chonotes.framework.datasource.cache.abstraction

import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.model.Note
import cho.chonotes.framework.datasource.cache.database.NOTE_PAGINATION_PAGE_SIZE

interface NoteDaoService {

    suspend fun insertNote(note: Note): Long

    suspend fun insertNotes(notes: List<Note>): LongArray

    suspend fun searchNoteById(id: String): Note?

    suspend fun updateNote(
        primaryKey: String,
        title: String,
        body: String?,
        folder_id: String?,
        timestamp: String?
    ): Int

    // added
    suspend fun moveNotes(
        notes: List<Note>,
        folder_id: String?,
        timestamp: String?
    ): Int

    suspend fun moveNotesToDefaultFolder(
        folder_id: String?,
        default_folder_id: String,
        timestamp: String?
    ): Int

    suspend fun moveNotesBackToRestore(
        folder_id: String?,
        default_folder_id: String,
        timestamp: String?
    ): Int

    suspend fun deleteNote(primaryKey: String): Int

    suspend fun deleteNotes(notes: List<Note>): Int

    suspend fun deleteNotesByFolderId(folderId: String): Int

    suspend fun searchNotes(): List<Note>

    suspend fun getAllNotes(currentUserID: String): List<Note>

    suspend fun getAllNotesByFolderId(folderId: String): List<Note>

    suspend fun getAllNotesByNoteFolderIds(folders: List<Folder>): List<Note>

    suspend fun searchNotesOrderByDateDESC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>

    suspend fun searchNotesOrderByDateASC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>

    suspend fun searchNotesOrderByTitleDESC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>

    suspend fun searchNotesOrderByTitleASC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<Note>

    suspend fun getNumNotes(): Int

    suspend fun returnOrderedQuery(
        uid: String,
        query: String,
        folderId: String,
        filterAndOrder: String,
        page: Int
    ): List<Note>
}












