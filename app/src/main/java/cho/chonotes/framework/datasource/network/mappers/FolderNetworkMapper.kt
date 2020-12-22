package cho.chonotes.framework.datasource.network.mappers

import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.business.domain.util.EntityMapper
import cho.chonotes.framework.datasource.network.model.FolderNetworkEntity
import javax.inject.Inject

class FolderNetworkMapper
@Inject
constructor(
    private val dateUtil: DateUtil
): EntityMapper<FolderNetworkEntity, Folder>
{

    fun entityListToFolderList(entities: List<FolderNetworkEntity>): List<Folder>{
        val list: ArrayList<Folder> = ArrayList()
        for(entity in entities){
            list.add(mapFromEntity(entity))
        }
        return list
    }

    override fun mapFromEntity(entity: FolderNetworkEntity): Folder {
        return Folder(
            folder_id = entity.folder_id,
            folder_name = entity.folder_name,
            notes_count = entity.notes_count,
            uid = entity.uid,
            updated_at = dateUtil.convertFirebaseTimestampToStringData(entity.updated_at),
            created_at = dateUtil.convertFirebaseTimestampToStringData(entity.created_at)
        )
    }

    override fun mapToEntity(domainModel: Folder): FolderNetworkEntity {
        return FolderNetworkEntity(
            folder_id = domainModel.folder_id,
            folder_name = domainModel.folder_name,
            notes_count = domainModel.notes_count,
            uid = domainModel.uid,
            updated_at = dateUtil.convertStringDateToFirebaseTimestamp(domainModel.updated_at),
            created_at = dateUtil.convertStringDateToFirebaseTimestamp(domainModel.created_at)
        )
    }


}







