package cho.chonotes.framework.datasource.cache.database

import androidx.lifecycle.LiveData
import androidx.room.*
import cho.chonotes.framework.datasource.cache.model.FolderCacheEntity
import cho.chonotes.framework.datasource.cache.model.FolderWithNotesCacheEntity

const val FOLDER_ORDER_ASC: String = ""
const val FOLDER_ORDER_DESC: String = "-"
const val FOLDER_FILTER_TITLE = "folder_name"
const val FOLDER_FILTER_DATE_CREATED = "created_at"

const val FOLDERS_ORDER_BY_ASC_DATE_UPDATED = FOLDER_ORDER_ASC + FOLDER_FILTER_DATE_CREATED
const val FOLDERS_ORDER_BY_DESC_DATE_UPDATED = FOLDER_ORDER_DESC + FOLDER_FILTER_DATE_CREATED
const val FOLDERS_ORDER_BY_ASC_TITLE = FOLDER_ORDER_ASC + FOLDER_FILTER_TITLE
const val FOLDERS_ORDER_BY_DESC_TITLE = FOLDER_ORDER_DESC + FOLDER_FILTER_TITLE

const val FOLDER_PAGINATION_PAGE_SIZE = 30

@Dao
interface FolderDao {

    @Query("""
        UPDATE folders 
        SET 
        folder_name = :newFolderName,
        updated_at = :updated_at
        WHERE folder_id = :primaryKey
        """)
    suspend fun renameFolder(
        primaryKey: String,
        newFolderName: String,
        updated_at: String
    ): Int

    @Insert
    suspend fun insertFolder(folder: FolderCacheEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolders(folders: List<FolderCacheEntity>): LongArray

    @Query("SELECT * FROM folders WHERE folder_id = :folder_id")
    suspend fun searchFolderById(folder_id: String): FolderCacheEntity?

    @Query("DELETE FROM folders WHERE folder_id IN (:ids)")
    suspend fun deleteFolders(ids: List<String>): Int

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()

    @Query("SELECT * FROM folders WHERE uid = :currentUserID")
    suspend fun getAllFolders(currentUserID: String): List<FolderCacheEntity>

    @Query("SELECT * FROM folders")
    @Transaction
    fun getAllFoldersWithNotes(): LiveData<List<FolderWithNotesCacheEntity>>

    @Query("""
        UPDATE folders 
        SET 
        folder_name = :folder_name,
        updated_at = :updated_at
        WHERE folder_id = :primaryKey
        """)
    suspend fun updateFolder(
        primaryKey: String,
        folder_name: String,
        updated_at: String
    ): Int

    @Query("DELETE FROM folders WHERE folder_id = :primaryKey")
    suspend fun deleteFolder(primaryKey: String): Int

    @Query("SELECT * FROM folders")
    @Transaction
    suspend fun searchFolders(): List<FolderWithNotesCacheEntity>

    @Query("SELECT COUNT(*) FROM folders")

    suspend fun getNumFolders(): Int

    @Query("""
        SELECT * FROM folders 
        WHERE folder_name LIKE '%' || :query || '%' 
        AND uid LIKE '%' || :uid || '%'
        ORDER BY updated_at DESC LIMIT (:page * :pageSize)
        """)
    @Transaction
    suspend fun searchFoldersWithNotesOrderByDateDESC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int = FOLDER_PAGINATION_PAGE_SIZE
    ): List<FolderWithNotesCacheEntity>

    @Query("""
        SELECT * FROM folders 
        WHERE folder_name LIKE '%' || :query || '%'
        AND uid LIKE '%' || :uid || '%'
        ORDER BY updated_at ASC LIMIT (:page * :pageSize)
        """)
    @Transaction
    suspend fun searchFoldersWithNotesOrderByDateASC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int = FOLDER_PAGINATION_PAGE_SIZE
    ): List<FolderWithNotesCacheEntity>

    @Query("""
        SELECT * FROM folders 
        WHERE folder_name LIKE '%' || :query || '%' 
        AND uid LIKE '%' || :uid || '%'
        ORDER BY folder_name DESC LIMIT (:page * :pageSize)
        """)
    @Transaction
    suspend fun searchFoldersWithNotesOrderByTitleDESC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int = FOLDER_PAGINATION_PAGE_SIZE
    ): List<FolderWithNotesCacheEntity>

    @Query("""
        SELECT * FROM folders 
        WHERE folder_name LIKE '%' || :query || '%' 
        AND uid LIKE '%' || :uid || '%'
        ORDER BY folder_name ASC LIMIT (:page * :pageSize)
        """)
    @Transaction
    suspend fun searchFoldersWithNotesOrderByTitleASC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int = FOLDER_PAGINATION_PAGE_SIZE
    ): List<FolderWithNotesCacheEntity>
}

suspend fun FolderDao.returnOrderedQueryWithNotes(
    uid: String,
    query: String,
    page: Int,
    filterAndOrder: String
): List<FolderWithNotesCacheEntity> {
    when{

        filterAndOrder.contains(FOLDERS_ORDER_BY_DESC_DATE_UPDATED) ->{
            return searchFoldersWithNotesOrderByDateDESC(
                uid = uid,
                query = query,
                page = page)
        }

        filterAndOrder.contains(FOLDERS_ORDER_BY_ASC_DATE_UPDATED) ->{
            return searchFoldersWithNotesOrderByDateASC(
                query = query,
                uid = uid,
                page = page)
        }

        filterAndOrder.contains(FOLDERS_ORDER_BY_DESC_TITLE) ->{
            return searchFoldersWithNotesOrderByTitleDESC(
                query = query,
                uid = uid,
                page = page)
        }

        filterAndOrder.contains(FOLDERS_ORDER_BY_ASC_TITLE) ->{
            return searchFoldersWithNotesOrderByTitleASC(
                query = query,
                uid = uid,
                page = page)
        }
        else ->
            return searchFoldersWithNotesOrderByDateDESC(
                query = query,
                uid = uid,
                page = page
            )
    }
}











