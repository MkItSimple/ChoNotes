package cho.chonotes.framework.datasource.cache.implementation

import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.framework.datasource.cache.abstraction.NoteDaoService
import cho.chonotes.framework.datasource.cache.database.NoteDao
import cho.chonotes.framework.datasource.cache.database.returnOrderedQuery
import cho.chonotes.framework.datasource.cache.mappers.CacheMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDaoServiceImpl
@Inject
constructor(
    private val noteDao: NoteDao,
    private val noteMapper: CacheMapper,
    private val dateUtil: DateUtil
): NoteDaoService {

    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(noteMapper.mapToEntity(note))
    }

    override suspend fun insertNotes(notes: List<Note>): LongArray {
        return noteDao.insertNotes(
            noteMapper.noteListToEntityList(notes)
        )
    }

    override suspend fun searchNoteById(id: String): Note? {
        return noteDao.searchNoteById(id)?.let { note ->
            noteMapper.mapFromEntity(note)
        }
    }

    override suspend fun updateNote(
        primaryKey: String,
        title: String,
        body: String?,
        note_folder_id: String?,
        timestamp: String?
    ): Int {
        return if(timestamp != null){
            noteDao.updateNote(
                primaryKey = primaryKey,
                title = title,
                body = body,
                note_folder_id = note_folder_id,
                updated_at = timestamp
            )
        }else{
            noteDao.updateNote(
                primaryKey = primaryKey,
                title = title,
                body = body,
                note_folder_id = note_folder_id,
                updated_at = dateUtil.getCurrentTimestamp()
            )
        }

    }

    override suspend fun moveNotes(
        notes: List<Note>,
        folder_id: String?,
        timestamp: String?
    ): Int {
        val ids = notes.mapIndexed { _, value -> value.note_id}
        return if(timestamp != null){

            noteDao.moveNotes(
                ids = ids,
                folder_id = folder_id,
                updated_at = timestamp
            )
        }else{
            noteDao.moveNotes(
                ids = ids,
                folder_id = folder_id,
                updated_at = dateUtil.getCurrentTimestamp()
            )
        }
    }

    override suspend fun moveNotesToDefaultFolder(
        folder_id: String?,
        default_folder_id: String,
        timestamp: String?
    ): Int {
        return if(timestamp != null){

            noteDao.moveNotesToDefaultFolder(
                folder_id = folder_id,
                default_folder_id = default_folder_id,
                updated_at = timestamp
            )
        }else{
            noteDao.moveNotesToDefaultFolder(
                folder_id = folder_id,
                default_folder_id = default_folder_id,
                updated_at = dateUtil.getCurrentTimestamp()
            )
        }
    }

    override suspend fun moveNotesBackToRestore(
        folder_id: String?,
        default_folder_id: String,
        timestamp: String?
    ): Int {
        return if(timestamp != null){

            noteDao.moveNotesBackToRestore(
                folder_id = folder_id,
                default_folder_id = default_folder_id,
                updated_at = timestamp
            )
        }else{
            noteDao.moveNotesBackToRestore(
                folder_id = folder_id,
                default_folder_id = default_folder_id,
                updated_at = dateUtil.getCurrentTimestamp()
            )
        }
    }

    override suspend fun deleteNote(primaryKey: String): Int {
        return noteDao.deleteNote(primaryKey)
    }

    override suspend fun deleteNotes(notes: List<Note>): Int {
        val ids = notes.mapIndexed { _, value -> value.note_id}
        return noteDao.deleteNotes(ids)
    }

    override suspend fun deleteNotesByFolderId(folderId: String): Int {
        return noteDao.deleteNotesByFolderId(folderId)
    }

    override suspend fun searchNotes(): List<Note> {
        return noteMapper.entityListToNoteList(
            noteDao.searchNotes()
        )
    }

    override suspend fun getAllNotes(currentUserID: String): List<Note> {
        return noteMapper.entityListToNoteList(noteDao.getAllNotes(currentUserID))
    }

    override suspend fun getAllNotesByFolderId(folderId: String): List<Note> {
        return noteMapper.entityListToNoteList(noteDao.getAllNotesByFolderId(folderId))
    }

    override suspend fun getAllNotesByNoteFolderIds(folders: List<Folder>): List<Note> {
        val ids = folders.mapIndexed { _, value -> value.folder_id }
        return noteMapper.entityListToNoteList(noteDao.getAllNotesByNoteFolderIds(ids))
    }

    override suspend fun searchNotesOrderByDateDESC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return noteMapper.entityListToNoteList(
            noteDao.searchNotesOrderByDateDESC(
                folderId = folderId,
                uid = uid,
                query = query,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchNotesOrderByDateASC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return noteMapper.entityListToNoteList(
            noteDao.searchNotesOrderByDateASC(
                uid = uid,
                query = query,
                folderId = folderId,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchNotesOrderByTitleDESC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return noteMapper.entityListToNoteList(
            noteDao.searchNotesOrderByTitleDESC(
                uid = uid,
                query = query,
                folderId = folderId,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchNotesOrderByTitleASC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int
    ): List<Note> {
        return noteMapper.entityListToNoteList(
            noteDao.searchNotesOrderByTitleASC(
                uid = uid,
                query = query,
                folderId = folderId,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun getNumNotes(): Int {
        return noteDao.getNumNotes()
    }

    override suspend fun returnOrderedQuery(
        uid: String,
        query: String,
        folderId: String,
        filterAndOrder: String,
        page: Int
    ): List<Note> {
        return noteMapper.entityListToNoteList(
            noteDao.returnOrderedQuery(
                uid = uid,
                query = query,
                folderId = folderId,
                page = page,
                filterAndOrder = filterAndOrder
            )
        )
    }
}













