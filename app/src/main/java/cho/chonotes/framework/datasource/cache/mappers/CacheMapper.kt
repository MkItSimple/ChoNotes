package cho.chonotes.framework.datasource.cache.mappers

import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.business.domain.util.EntityMapper
import cho.chonotes.framework.datasource.cache.model.NoteCacheEntity
import javax.inject.Inject

/**
 * Maps Note to NoteCacheEntity or NoteCacheEntity to Note.
 */
class CacheMapper
@Inject
constructor(
    private val dateUtil: DateUtil
): EntityMapper<NoteCacheEntity, Note>
{

    fun entityListToNoteList(entities: List<NoteCacheEntity>): List<Note>{
        val list: ArrayList<Note> = ArrayList()
        for(entity in entities){
            list.add(mapFromEntity(entity))
        }
        return list
    }

    fun noteListToEntityList(notes: List<Note>): List<NoteCacheEntity>{
        val entities: ArrayList<NoteCacheEntity> = ArrayList()
        for(note in notes){
            entities.add(mapToEntity(note))
        }
        return entities
    }

    override fun mapFromEntity(entity: NoteCacheEntity): Note {
        return Note(
            note_id = entity.note_id,
            title = entity.title,
            body = entity.body,
            note_folder_id = entity.note_folder_id,
            uid = entity.uid,
            updated_at = entity.updated_at,
            created_at = entity.created_at
        )
    }

    override fun mapToEntity(domainModel: Note): NoteCacheEntity {
        return NoteCacheEntity(
            note_id = domainModel.note_id,
            title = domainModel.title,
            body = domainModel.body,
            note_folder_id = domainModel.note_folder_id,
            uid = domainModel.uid,
            updated_at = domainModel.updated_at,
            created_at = domainModel.created_at
        )
    }
}







