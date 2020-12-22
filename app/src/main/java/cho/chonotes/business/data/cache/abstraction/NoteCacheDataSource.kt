package cho.chonotes.business.data.cache.abstraction

import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.model.Note

interface NoteCacheDataSource{

    suspend fun insertNote(note: Note): Long

    suspend fun deleteNote(primaryKey: String): Int

    suspend fun deleteNotes(notes: List<Note>): Int

    suspend fun deleteNotesByFolderId(folderId: String): Int

    suspend fun updateNote(
        primaryKey: String,
        newTitle: String,
        newBody: String?,
        newFolderId: String?,
        timestamp: String?
    ): Int

    suspend fun moveNotes(
        notes: List<Note>,
        newFolderId: String?,
        timestamp: String?
    ): Int

    suspend fun moveNotesToDefaultFolder(
        folderId: String?,
        defaultFolderId: String,
        timestamp: String?
    ): Int

    suspend fun moveNotesBackToRestore(
        folderId: String?,
        defaultFolderId: String,
        timestamp: String?
    ): Int

    // for testing
    suspend fun searchNotes(
        uid: String,
        query: String,
        folderId: String,
        filterAndOrder: String,
        page: Int
    ): List<Note>

    suspend fun getAllNotes(currentUserID: String): List<Note>

    suspend fun getAllNotesByFolderId(folderId: String): List<Note>

    suspend fun getAllNotesByNoteFolderIds(folders: List<Folder>): List<Note>

    suspend fun searchNoteById(id: String): Note?

    suspend fun getNumNotes(): Int

    suspend fun insertNotes(notes: List<Note>): LongArray
}






