package cho.chonotes.framework.datasource.cache.abstraction

import cho.chonotes.business.domain.model.Folder
import cho.chonotes.framework.datasource.cache.database.FOLDER_PAGINATION_PAGE_SIZE

interface FolderDaoService {

    suspend fun renameFolder(
        primaryKey: String,
        newFolderName: String,
        timestamp: String?
    ): Int

    suspend fun insertFolder(folder: Folder): Long

    suspend fun insertFolders(folders: List<Folder>): LongArray

    suspend fun searchFolderById(id: String): Folder?

    suspend fun updateFolder(
        primaryKey: String,
        folder_name: String,
        notes_count: Int?,
        timestamp: String?
    ): Int

    suspend fun deleteFolder(primaryKey: String): Int

    suspend fun deleteFolders(folders: List<Folder>): Int

    suspend fun searchFolders(): List<Folder>

    suspend fun getAllFolders(currentUserID: String): List<Folder>

    suspend fun getNumFolders(): Int

    suspend fun searchFoldersWithNotesOrderByDateDESC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int = FOLDER_PAGINATION_PAGE_SIZE
    ): List<Folder>

    suspend fun searchFoldersWithNotesOrderByDateASC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int = FOLDER_PAGINATION_PAGE_SIZE
    ): List<Folder>

    suspend fun searchFoldersWithNotesOrderByTitleDESC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int = FOLDER_PAGINATION_PAGE_SIZE
    ): List<Folder>

    suspend fun searchFoldersWithNotesOrderByTitleASC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int = FOLDER_PAGINATION_PAGE_SIZE
    ): List<Folder>

    suspend fun returnOrderedQueryWithNotes(
        uid: String,
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Folder>
}












