package cho.chonotes.framework.datasource.network.abstraction

import cho.chonotes.business.domain.model.Folder

interface FolderFirestoreService {

    suspend fun insertOrUpdateFolder(folder: Folder)

    suspend fun insertOrUpdateFolders(folders: List<Folder>)

    suspend fun deleteFolder(primaryKey: String)

    suspend fun insertDeletedFolder(folder: Folder)

    suspend fun insertDeletedFolders(folders: List<Folder>)

    suspend fun deleteDeletedFolder(folder: Folder)

    suspend fun deleteAllFolders()

    suspend fun getDeletedFolders(): List<Folder>

    suspend fun searchFolder(folder: Folder): Folder?

    suspend fun getAllFolders(): List<Folder>


}