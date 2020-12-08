package cho.chonotes.framework.datasource.cache.mappers

import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.model.FolderWithNotes
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.business.domain.util.EntityMapper
import cho.chonotes.business.domain.util.TwoEntityMapper
import cho.chonotes.framework.datasource.cache.model.FolderCacheEntity
import cho.chonotes.framework.datasource.cache.model.FolderWithNotesCacheEntity
import javax.inject.Inject

/**
 * Maps Folder to FolderCacheEntity or FolderCacheEntity to Folder.
 */
class FolderWithNotesCacheMapper
@Inject
constructor(
    private val dateUtil: DateUtil
): TwoEntityMapper<FolderWithNotesCacheEntity, Folder>
{

    fun entityListToFolderList(entities: List<FolderWithNotesCacheEntity>): List<Folder>{
        val list: ArrayList<Folder> = ArrayList()
        for(entity in entities){
            list.add(mapFromEntity(entity))
        }
        return list
    }

//    fun folderListToEntityList(folders: List<Folder>): List<FolderWithNotesCacheEntity>{
//        val entities: ArrayList<FolderCacheEntity> = ArrayList()
//        for(folder in folders){
//            entities.add(mapToEntity(folder))
//        }
//        return entities
//    }

    override fun mapFromEntity(entity: FolderWithNotesCacheEntity): Folder {
        return Folder(
            folder_id = entity.folderCacheEntity.folder_id,
            folder_name = entity.folderCacheEntity.folder_name,
            notes_count = entity.notes.size,
            uid = entity.folderCacheEntity.uid,
            updated_at = entity.folderCacheEntity.updated_at,
            created_at = entity.folderCacheEntity.created_at
        )
    }
}







