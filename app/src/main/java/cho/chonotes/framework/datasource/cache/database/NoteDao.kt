package cho.chonotes.framework.datasource.cache.database

import androidx.room.*
import cho.chonotes.framework.datasource.cache.model.NoteCacheEntity

const val NOTE_ORDER_ASC: String = ""
const val NOTE_ORDER_DESC: String = "-"
const val NOTE_FILTER_TITLE = "title"
const val NOTE_FILTER_DATE_CREATED = "created_at"

const val ORDER_BY_ASC_DATE_UPDATED = NOTE_ORDER_ASC + NOTE_FILTER_DATE_CREATED
const val ORDER_BY_DESC_DATE_UPDATED = NOTE_ORDER_DESC + NOTE_FILTER_DATE_CREATED
const val ORDER_BY_ASC_TITLE = NOTE_ORDER_ASC + NOTE_FILTER_TITLE
const val ORDER_BY_DESC_TITLE = NOTE_ORDER_DESC + NOTE_FILTER_TITLE

const val NOTE_PAGINATION_PAGE_SIZE = 30

@Dao
interface NoteDao {

    @Insert
    suspend fun insertNote(note: NoteCacheEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotes(notes: List<NoteCacheEntity>): LongArray

    @Query("SELECT * FROM notes WHERE note_id = :note_id")
    suspend fun searchNoteById(note_id: String): NoteCacheEntity?

    @Query("DELETE FROM notes WHERE note_id IN (:ids)")
    suspend fun deleteNotes(ids: List<String>): Int

    @Query("DELETE FROM notes WHERE note_folder_id = :folderId")
    suspend fun deleteNotesByFolderId(folderId: String): Int

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

//    // for syncing
//    @Query("SELECT * FROM notes WHERE uid = :currentUserID")
//    suspend fun getAllNotes(currentUserID: String): List<NoteCacheEntity>

    // for syncing
    @Query("SELECT * FROM notes WHERE uid = :currentUserID")
    suspend fun getAllNotes(currentUserID: String): List<NoteCacheEntity>

    @Query("SELECT * FROM notes WHERE note_folder_id = :folderId")
    suspend fun getAllNotesByFolderId(folderId: String): List<NoteCacheEntity>

    @Query("""
        SELECT * FROM notes
        WHERE note_folder_id IN (:ids)
        """)
    suspend fun getAllNotesByNoteFolderIds(ids: List<String>): List<NoteCacheEntity>

    @Query("""
        UPDATE notes 
        SET 
        title = :title, 
        body = :body,
        note_folder_id = :note_folder_id,
        updated_at = :updated_at
        WHERE note_id = :primaryKey
        """)
    suspend fun updateNote(
        primaryKey: String,
        title: String,
        body: String?,
        note_folder_id: String?,
        updated_at: String
    ): Int

    // added
    @Query("""
        UPDATE notes 
        SET
        note_folder_id = :folder_id,
        updated_at = :updated_at
        WHERE note_id IN (:ids)
        """)
    suspend fun moveNotes(
        ids: List<String>,
        folder_id: String?,
        updated_at: String
    ): Int

    // added
    @Query("""
        UPDATE notes 
        SET
        note_folder_id = :default_folder_id,
        updated_at = :updated_at
        WHERE note_folder_id = :folder_id
        """)
    suspend fun moveNotesToDefaultFolder(
        folder_id: String?,
        default_folder_id: String,
        updated_at: String
    ): Int

    @Query("""
        UPDATE notes 
        SET
        note_folder_id = :folder_id,
        updated_at = :updated_at
        WHERE note_folder_id = :default_folder_id
        """)
    suspend fun moveNotesBackToRestore(
        folder_id: String?,
        default_folder_id: String,
        updated_at: String
    ): Int

    @Query("DELETE FROM notes WHERE note_id = :primaryKey")
    suspend fun deleteNote(primaryKey: String): Int

    @Query("SELECT * FROM notes")
    suspend fun searchNotes(): List<NoteCacheEntity>


    @Query("""
        SELECT * FROM notes 
        WHERE (title LIKE '%' || :query || '%' AND note_folder_id LIKE '%' || :folderId || '%' AND uid = :uid)
        OR (body LIKE '%' || :query || '%' AND note_folder_id LIKE '%' || :folderId || '%' AND uid = :uid)
        ORDER BY updated_at DESC LIMIT (:page * :pageSize)
        """)
    suspend fun searchNotesOrderByDateDESC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<NoteCacheEntity>

    @Query("""
        SELECT * FROM notes 
        WHERE (title LIKE '%' || :query || '%' AND note_folder_id LIKE '%' || :folderId || '%' AND uid LIKE '%' || :uid || '%')
        OR (body LIKE '%' || :query || '%' AND note_folder_id LIKE '%' || :folderId || '%' AND uid LIKE '%' || :uid || '%')
        ORDER BY updated_at ASC LIMIT (:page * :pageSize)
        """)
    suspend fun searchNotesOrderByDateASC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<NoteCacheEntity>

    @Query("""
        SELECT * FROM notes 
        WHERE (title LIKE '%' || :query || '%' AND note_folder_id LIKE '%' || :folderId || '%' AND uid LIKE '%' || :uid || '%')
        OR (body LIKE '%' || :query || '%' AND note_folder_id LIKE '%' || :folderId || '%' AND uid LIKE '%' || :uid || '%')
        ORDER BY title DESC LIMIT (:page * :pageSize)
        """)
    suspend fun searchNotesOrderByTitleDESC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<NoteCacheEntity>

    @Query("""
        SELECT * FROM notes 
        WHERE (title LIKE '%' || :query || '%' AND note_folder_id LIKE '%' || :folderId || '%' AND uid LIKE '%' || :uid || '%')
        OR (body LIKE '%' || :query || '%' AND note_folder_id LIKE '%' || :folderId || '%' AND uid LIKE '%' || :uid || '%')
        ORDER BY title ASC LIMIT (:page * :pageSize)
        """)
    suspend fun searchNotesOrderByTitleASC(
        uid: String,
        query: String,
        folderId: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): List<NoteCacheEntity>


    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNumNotes(): Int
}


suspend fun NoteDao.returnOrderedQuery(
    uid: String,
    query: String,
    folderId: String,
    filterAndOrder: String,
    page: Int
): List<NoteCacheEntity> {

    when{

        filterAndOrder.contains(ORDER_BY_DESC_DATE_UPDATED) ->{
            return searchNotesOrderByDateDESC(
                uid = uid,
                query = query,
                folderId = folderId,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_ASC_DATE_UPDATED) ->{
            return searchNotesOrderByDateASC(
                uid = uid,
                query = query,
                folderId = folderId,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_DESC_TITLE) ->{
            return searchNotesOrderByTitleDESC(
                uid = uid,
                query = query,
                folderId = folderId,
                page = page)
        }

        filterAndOrder.contains(ORDER_BY_ASC_TITLE) ->{
            return searchNotesOrderByTitleASC(
                uid = uid,
                query = query,
                folderId = folderId,
                page = page)
        }
        else ->
            return searchNotesOrderByDateDESC(
                uid = uid,
                query = query,
                folderId = folderId,
                page = page
            )
    }
}












