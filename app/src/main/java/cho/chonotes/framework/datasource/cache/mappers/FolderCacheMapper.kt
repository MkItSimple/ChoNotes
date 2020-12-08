package cho.chonotes.framework.datasource.cache.mappers

import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.business.domain.util.EntityMapper
import cho.chonotes.framework.datasource.cache.model.FolderCacheEntity
import javax.inject.Inject

/**
 * Maps Folder to FolderCacheEntity or FolderCacheEntity to Folder.
 */
class FolderCacheMapper
@Inject
constructor(
    private val dateUtil: DateUtil
): EntityMapper<FolderCacheEntity, Folder>
{

    fun entityListToFolderList(entities: List<FolderCacheEntity>): List<Folder>{
        val list: ArrayList<Folder> = ArrayList()
        for(entity in entities){
            list.add(mapFromEntity(entity))
        }
        return list
    }

    fun folderListToEntityList(folders: List<Folder>): List<FolderCacheEntity>{
        val entities: ArrayList<FolderCacheEntity> = ArrayList()
        for(folder in folders){
            entities.add(mapToEntity(folder))
        }
        return entities
    }

    override fun mapFromEntity(entity: FolderCacheEntity): Folder {
        return Folder(
            folder_id = entity.folder_id,
            folder_name = entity.folder_name,
            notes_count = entity.notes_count,
            uid = entity.uid,
            updated_at = entity.updated_at,
            created_at = entity.created_at
        )
    }

    override fun mapToEntity(domainModel: Folder): FolderCacheEntity {
        return FolderCacheEntity(
            folder_id = domainModel.folder_id,
            folder_name = domainModel.folder_name,
            notes_count = domainModel.notes_count,
            uid = domainModel.uid,
            updated_at = domainModel.updated_at,
            created_at = domainModel.created_at
        )
    }
}







