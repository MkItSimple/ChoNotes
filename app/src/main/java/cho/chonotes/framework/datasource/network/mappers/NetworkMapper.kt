package cho.chonotes.framework.datasource.network.mappers

import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.business.domain.util.EntityMapper
import cho.chonotes.framework.datasource.network.model.NoteNetworkEntity
import javax.inject.Inject

class NetworkMapper
@Inject
constructor(
    private val dateUtil: DateUtil
): EntityMapper<NoteNetworkEntity, Note>
{
    fun entityListToNoteList(entities: List<NoteNetworkEntity>): List<Note>{
        val list: ArrayList<Note> = ArrayList()
        for(entity in entities){
            list.add(mapFromEntity(entity))
        }
        return list
    }

    // for testing . . NoteFirestoreServiceTest
    fun noteListToEntityList(notes: List<Note>): List<NoteNetworkEntity>{
        val entities: ArrayList<NoteNetworkEntity> = ArrayList()
        for(note in notes){
            entities.add(mapToEntity(note))
        }
        return entities
    }


    override fun mapFromEntity(entity: NoteNetworkEntity): Note {
        return Note(
            note_id = entity.note_id,
            title = entity.title,
            body = entity.body,
            note_folder_id = entity.note_folder_id,
            uid = entity.uid,
            updated_at = dateUtil.convertFirebaseTimestampToStringData(entity.updated_at),
            created_at = dateUtil.convertFirebaseTimestampToStringData(entity.created_at)
        )
    }

    override fun mapToEntity(domainModel: Note): NoteNetworkEntity {
        return NoteNetworkEntity(
            note_id = domainModel.note_id,
            title = domainModel.title,
            body = domainModel.body,
            note_folder_id = domainModel.note_folder_id,
            uid = domainModel.uid,
            updated_at = dateUtil.convertStringDateToFirebaseTimestamp(domainModel.updated_at),
            created_at = dateUtil.convertStringDateToFirebaseTimestamp(domainModel.created_at)
        )
    }
}







