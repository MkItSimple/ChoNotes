package cho.chonotes.business.data.network.implementation

import cho.chonotes.business.data.network.abstraction.FolderNetworkDataSource
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.framework.datasource.network.abstraction.FolderFirestoreService
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FolderNetworkDataSourceImpl
@Inject
constructor(
    private val firestoreService: FolderFirestoreService
): FolderNetworkDataSource {

    override suspend fun insertOrUpdateFolder(folder: Folder) {
        return firestoreService.insertOrUpdateFolder(folder)
    }

    override suspend fun deleteFolder(primaryKey: String) {
        return firestoreService.deleteFolder(primaryKey)
    }

    override suspend fun insertDeletedFolder(folder: Folder) {
        return firestoreService.insertDeletedFolder(folder)
    }

    override suspend fun insertDeletedFolders(folders: List<Folder>) {
        return firestoreService.insertDeletedFolders(folders)
    }

    override suspend fun deleteDeletedFolder(folder: Folder) {
        return firestoreService.deleteDeletedFolder(folder)
    }

    override suspend fun getDeletedFolders(): List<Folder> {
        return firestoreService.getDeletedFolders()
    }

    override suspend fun deleteAllFolders() {
        firestoreService.deleteAllFolders()
    }

    override suspend fun searchFolder(folder: Folder): Folder? {
        return firestoreService.searchFolder(folder)
    }

    override suspend fun getAllFolders(): List<Folder> {
        return firestoreService.getAllFolders()
    }

    override suspend fun insertOrUpdateFolders(folders: List<Folder>) {
        return firestoreService.insertOrUpdateFolders(folders)
    }


}





























