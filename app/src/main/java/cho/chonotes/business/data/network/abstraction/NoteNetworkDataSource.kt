package cho.chonotes.business.data.network.abstraction

import cho.chonotes.business.domain.model.Note


interface NoteNetworkDataSource{

    suspend fun insertOrUpdateNote(note: Note)

    suspend fun deleteNote(note: Note)

    suspend fun insertDeletedNote(note: Note)

    suspend fun insertDeletedNotes(notes: List<Note>)

    suspend fun deleteDeletedNote(note: Note)

    suspend fun getDeletedNotes(): List<Note>

    suspend fun deleteAllNotes()

    suspend fun searchNote(note: Note): Note?

    suspend fun getAllNotes(): List<Note>

    suspend fun insertOrUpdateNotes(notes: List<Note>)

    suspend fun moveNotes(notes: List<Note>, folderId: String)



}
