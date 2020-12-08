package cho.chonotes.framework.datasource.cache.implementation

import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.framework.datasource.cache.abstraction.FolderDaoService
import cho.chonotes.framework.datasource.cache.database.FolderDao
import cho.chonotes.framework.datasource.cache.database.returnOrderedQueryWithNotes
import cho.chonotes.framework.datasource.cache.mappers.FolderCacheMapper
import cho.chonotes.framework.datasource.cache.mappers.FolderWithNotesCacheMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderDaoServiceImpl
@Inject
constructor(
    private val folderDao: FolderDao,
    private val folderMapper: FolderCacheMapper,
    private val folderWithNotesMapper: FolderWithNotesCacheMapper,
    private val dateUtil: DateUtil
): FolderDaoService {

    override suspend fun renameFolder(
        primaryKey: String,
        newFolderName: String,
        timestamp: String?
    ): Int {
        return if(timestamp != null){
            folderDao.renameFolder(
                primaryKey = primaryKey,
                newFolderName = newFolderName,
                updated_at = timestamp
            )
        }else{
            folderDao.renameFolder(
                primaryKey = primaryKey,
                newFolderName = newFolderName,
                updated_at = dateUtil.getCurrentTimestamp()
            )
        }

    }

    override suspend fun insertFolder(folder: Folder): Long {
        return folderDao.insertFolder(folderMapper.mapToEntity(folder))
    }

    override suspend fun insertFolders(folders: List<Folder>): LongArray {
        return folderDao.insertFolders(
            folderMapper.folderListToEntityList(folders)
        )
    }

    override suspend fun searchFolderById(id: String): Folder? {
        return folderDao.searchFolderById(id)?.let { folder ->
            folderMapper.mapFromEntity(folder)
        }
    }

    override suspend fun updateFolder(
        primaryKey: String,
        folder_name: String,
        notes_count: Int?,
        timestamp: String?
    ): Int {
        return if(timestamp != null){
            folderDao.updateFolder(
                primaryKey = primaryKey,
                folder_name = folder_name,
                updated_at = timestamp
            )
        }else{
            folderDao.updateFolder(
                primaryKey = primaryKey,
                folder_name = folder_name,
                updated_at = dateUtil.getCurrentTimestamp()
            )
        }

    }

    override suspend fun deleteFolder(primaryKey: String): Int {
        return folderDao.deleteFolder(primaryKey)
    }

    override suspend fun deleteFolders(folders: List<Folder>): Int {
        val ids = folders.mapIndexed {_, value -> value.folder_id}
        return folderDao.deleteFolders(ids)
    }

    override suspend fun searchFolders(): List<Folder> {
        return folderWithNotesMapper.entityListToFolderList(
            folderDao.searchFolders()
        )
    }

    override suspend fun getAllFolders(currentUserID: String): List<Folder> {
        return folderMapper.entityListToFolderList(folderDao.getAllFolders(currentUserID))
    }

//    override suspend fun searchFoldersOrderByDateDESC(
//        query: String,
//        page: Int,
//        pageSize: Int
//    ): List<Folder> {
//        return folderMapper.entityListToFolderList(
//            folderDao.searchFoldersOrderByDateDESC(
//                query = query,
//                page = page,
//                pageSize = pageSize
//            )
//        )
//    }
//
//    override suspend fun searchFoldersOrderByDateASC(
//        query: String,
//        page: Int,
//        pageSize: Int
//    ): List<Folder> {
//        return folderMapper.entityListToFolderList(
//            folderDao.searchFoldersOrderByDateASC(
//                query = query,
//                page = page,
//                pageSize = pageSize
//            )
//        )
//    }
//
//    override suspend fun searchFoldersOrderByTitleDESC(
//        query: String,
//        page: Int,
//        pageSize: Int
//    ): List<Folder> {
//        return folderMapper.entityListToFolderList(
//            folderDao.searchFoldersOrderByTitleDESC(
//                query = query,
//                page = page,
//                pageSize = pageSize
//            )
//        )
//    }
//
//    override suspend fun searchFoldersOrderByTitleASC(
//        query: String,
//        page: Int,
//        pageSize: Int
//    ): List<Folder> {
//        return folderMapper.entityListToFolderList(
//            folderDao.searchFoldersOrderByTitleASC(
//                query = query,
//                page = page,
//                pageSize = pageSize
//            )
//        )
//    }

    override suspend fun getNumFolders(): Int {
        return folderDao.getNumFolders()
    }

//    override suspend fun returnOrderedQuery(
//        query: String,
//        filterAndOrder: String,
//        page: Int
//    ): List<Folder> {
//        return folderMapper.entityListToFolderList(
//            folderDao.returnOrderedQuery(
//                query = query,
//                page = page,
//                filterAndOrder = filterAndOrder
//            )
//        )
//    }

    // FoldersWithNotes
    override suspend fun searchFoldersWithNotesOrderByDateDESC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int
    ): List<Folder> {
        return folderWithNotesMapper.entityListToFolderList(
            folderDao.searchFoldersWithNotesOrderByDateDESC(
                uid = uid,
                query = query,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchFoldersWithNotesOrderByDateASC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int
    ): List<Folder> {
        return folderWithNotesMapper.entityListToFolderList(
            folderDao.searchFoldersWithNotesOrderByDateASC(
                query = query,
                uid = uid,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchFoldersWithNotesOrderByTitleDESC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int
    ): List<Folder> {
        return folderWithNotesMapper.entityListToFolderList(
            folderDao.searchFoldersWithNotesOrderByTitleDESC(
                query = query,
                uid = uid,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchFoldersWithNotesOrderByTitleASC(
        uid: String,
        query: String,
        page: Int,
        pageSize: Int
    ): List<Folder> {
        return folderWithNotesMapper.entityListToFolderList(
            folderDao.searchFoldersWithNotesOrderByTitleASC(
                uid = uid,
                query = query,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun returnOrderedQueryWithNotes(
        uid: String,
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Folder> {
        return folderWithNotesMapper.entityListToFolderList(
            folderDao.returnOrderedQueryWithNotes(
                uid = uid,
                query = query,
                page = page,
                filterAndOrder = filterAndOrder
            )
        )
    }
}













