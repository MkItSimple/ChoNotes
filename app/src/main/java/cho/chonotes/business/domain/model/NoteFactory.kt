package cho.chonotes.business.domain.model

import cho.chonotes.business.domain.util.DateUtil
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteFactory
@Inject
constructor(
    private val dateUtil: DateUtil
) {

    fun createSingleNote(
        note_id: String? = null,
        title: String,
        body: String? = null,
        note_folder_id: String? = null,
        uid: String? = null
    ): Note {
        return Note(
            note_id = note_id ?: UUID.randomUUID().toString(),
            title = title,
            body = body ?: "",
            note_folder_id = note_folder_id ?: DEFAULT_FOLDER_ID,
            uid = uid?: "",
            created_at = dateUtil.getCurrentTimestamp(),
            updated_at = dateUtil.getCurrentTimestamp()
        )
    }

    fun createNoteList(numNotes: Int): List<Note> {
        val list: ArrayList<Note> = ArrayList()
        for(i in 0 until numNotes){ // exclusive on upper bound
            list.add(
                createSingleNote(
                    note_id = UUID.randomUUID().toString(),
                    title = UUID.randomUUID().toString(),
                    body = UUID.randomUUID().toString(),
                    note_folder_id = UUID.randomUUID().toString(),
                    uid = UUID.randomUUID().toString()
                )
            )
        }
        return list
    }

    companion object{
        const val DEFAULT_FOLDER_ID = "notes"
    }
}









