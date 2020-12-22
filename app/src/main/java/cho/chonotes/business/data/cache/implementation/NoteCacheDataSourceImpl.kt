package cho.chonotes.business.data.cache.implementation

import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.model.Note
import cho.chonotes.framework.datasource.cache.abstraction.NoteDaoService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteCacheDataSourceImpl
@Inject
constructor(
    private val noteDaoService: NoteDaoService
): NoteCacheDataSource {

    override suspend fun insertNote(note: Note): Long {
        return noteDaoService.insertNote(note)
    }

    override suspend fun deleteNote(primaryKey: String): Int {
        return noteDaoService.deleteNote(primaryKey)
    }

    override suspend fun deleteNotes(notes: List<Note>): Int {
        return noteDaoService.deleteNotes(notes)
    }

    override suspend fun deleteNotesByFolderId(folderId: String): Int {
        return noteDaoService.deleteNotesByFolderId(folderId)
    }

    override suspend fun updateNote(
        primaryKey: String,
        newTitle: String,
        newBody: String?,
        newFolderId: String?,
        timestamp: String?
    ): Int {
        return noteDaoService.updateNote(
            primaryKey,
            newTitle,
            newBody,
            newFolderId,
            timestamp
        )
    }
    
    override suspend fun moveNotes(
        notes: List<Note>,
        newFolderId: String?,
        timestamp: String?
    ): Int {
        return noteDaoService.moveNotes(
            notes,
            newFolderId,
            timestamp
        )
    }

    override suspend fun moveNotesToDefaultFolder(
        folderId: String?,
        defaultFolderId: String,
        timestamp: String?
    ): Int {
        return noteDaoService.moveNotesToDefaultFolder(
            folderId,
            defaultFolderId,
            timestamp
        )
    }

    override suspend fun moveNotesBackToRestore(
        folderId: String?,
        defaultFolderId: String,
        timestamp: String?
    ): Int {
        return noteDaoService.moveNotesBackToRestore(
            folderId,
            defaultFolderId,
            timestamp
        )
    }

    override suspend fun searchNotes(
        uid: String,
        query: String,
        folderId: String,
        filterAndOrder: String,
        page: Int
    ): List<Note> {
        return noteDaoService.returnOrderedQuery(
            uid, query, folderId, filterAndOrder, page
        )
    }

    override suspend fun getAllNotes(currentUserID: String): List<Note> {
        return noteDaoService.getAllNotes(currentUserID)
    }

    override suspend fun getAllNotesByFolderId(folderId: String): List<Note> {
        return noteDaoService.getAllNotesByFolderId(folderId)
    }

    override suspend fun getAllNotesByNoteFolderIds(folders: List<Folder>): List<Note> {
        return noteDaoService.getAllNotesByNoteFolderIds(folders)
    }

    override suspend fun searchNoteById(id: String): Note? {
        return noteDaoService.searchNoteById(id)
    }

    override suspend fun getNumNotes(): Int {
        return noteDaoService.getNumNotes()
    }

    override suspend fun insertNotes(notes: List<Note>): LongArray{
        return noteDaoService.insertNotes(notes)
    }
}





















