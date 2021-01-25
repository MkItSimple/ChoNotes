package cho.chonotes.business.data.network

import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.util.DateUtil

class FakeNoteNetworkDataSourceImpl
constructor(
    private val notesData: HashMap<String, Note>,
    private val deletedNotesData: HashMap<String, Note>,
    private val dateUtil: DateUtil
) : NoteNetworkDataSource{

    override suspend fun insertOrUpdateNote(note: Note) {
        val n = Note(
            note_id = note.note_id,
            title = note.title,
            body = note.body,
            note_folder_id = note.note_folder_id,
            uid = note.uid,
            created_at = note.created_at,
            updated_at = dateUtil.getCurrentTimestamp()
        )
        notesData.put(note.note_id, n)
    }

    override suspend fun deleteNote(note: Note) {
        notesData.remove(note.note_id)
    }

    override suspend fun insertDeletedNote(note: Note) {
        deletedNotesData.put(note.note_id, note)
    }

    override suspend fun insertDeletedNotes(notes: List<Note>) {
        for(note in notes){
            deletedNotesData.put(note.note_id, note)
        }
    }

    override suspend fun deleteDeletedNote(note: Note) {
        deletedNotesData.remove(note.note_id)
    }

    override suspend fun getDeletedNotes(): List<Note> {
        return ArrayList(deletedNotesData.values)
    }

    override suspend fun deleteAllNotes() {
        deletedNotesData.clear()
    }

    override suspend fun searchNote(note: Note): Note? {
        return notesData.get(note.note_id)
    }

    override suspend fun getAllNotes(): List<Note> {
        return ArrayList(notesData.values)
    }

    override suspend fun insertOrUpdateNotes(notes: List<Note>) {
        for(note in notes){
            notesData.put(note.note_id, note)
        }
    }

    override suspend fun moveNotes(notes: List<Note>, folderId: String) {
        TODO("Not yet implemented")
    }
}
