package cho.chonotes.business.data.network.implementation

import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.Note
import cho.chonotes.framework.datasource.network.abstraction.NoteFirestoreService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteNetworkDataSourceImpl
@Inject
constructor(
    private val firestoreService: NoteFirestoreService
): NoteNetworkDataSource {

    override suspend fun insertOrUpdateNote(note: Note) {
        return firestoreService.insertOrUpdateNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        return firestoreService.deleteNote(note)
    }

    override suspend fun insertDeletedNote(note: Note) {
        return firestoreService.insertDeletedNote(note)
    }

    override suspend fun insertDeletedNotes(notes: List<Note>) {
        return firestoreService.insertDeletedNotes(notes)
    }

    override suspend fun deleteDeletedNote(note: Note) {
        return firestoreService.deleteDeletedNote(note)
    }

    override suspend fun getDeletedNotes(): List<Note> {
        return firestoreService.getDeletedNotes()
    }

    override suspend fun deleteAllNotes() {
        firestoreService.deleteAllNotes()
    }

    override suspend fun searchNote(note: Note): Note? {
        return firestoreService.searchNote(note)
    }

    override suspend fun getAllNotes(): List<Note> {
        return firestoreService.getAllNotes()
    }

    override suspend fun insertOrUpdateNotes(nl: List<Note>) {
        return firestoreService.insertOrUpdateNotes(nl)
    }

    override suspend fun moveNotes(l: List<Note>, folderId: String) {
        return firestoreService.moveNotes(l, folderId)
    }
}





























