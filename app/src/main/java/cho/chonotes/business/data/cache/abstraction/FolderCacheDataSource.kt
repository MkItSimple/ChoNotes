package cho.chonotes.business.data.cache.abstraction

import cho.chonotes.business.domain.model.Folder
import cho.chonotes.framework.datasource.cache.model.FolderWithNotesCacheEntity

interface FolderCacheDataSource{

    suspend fun renameFolder(
        primaryKey: String,
        newFolderName: String,
        timestamp: String?
    ): Int

    suspend fun insertFolder(folder: Folder): Long

    suspend fun deleteFolder(primaryKey: String): Int

    suspend fun deleteFolders(folders: List<Folder>): Int

    suspend fun updateFolder(
        primaryKey: String,
        newFolderName: String,
        newNotesCount: Int?,
        timestamp: String?
    ): Int

    suspend fun searchFoldersWithNotes(
        uid: String,
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Folder>

    suspend fun getAllFolders(currentUserID: String): List<Folder>

    suspend fun searchFolderById(id: String): Folder?

    suspend fun getNumFolders(): Int

    suspend fun insertFolders(folders: List<Folder>): LongArray

}






