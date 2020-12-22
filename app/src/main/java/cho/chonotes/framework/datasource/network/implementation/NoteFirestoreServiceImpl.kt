package cho.chonotes.framework.datasource.network.implementation

import android.util.Log
import cho.chonotes.business.domain.model.Note
import cho.chonotes.framework.datasource.network.abstraction.NoteFirestoreService
import cho.chonotes.framework.datasource.network.mappers.NetworkMapper
import cho.chonotes.framework.datasource.network.model.NoteNetworkEntity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteFirestoreServiceImpl
@Inject
constructor(
    private val firestore: FirebaseFirestore,
    private val networkMapper: NetworkMapper
): NoteFirestoreService {

//    private val currentUserID = FirebaseAuth.getInstance().currentUser?.uid?.let {
//        it
//    } ?: ""

    override suspend fun insertOrUpdateNote(note: Note) {

            val entity = networkMapper.mapToEntity(note)
            entity.updated_at = Timestamp.now()
            firestore
                .collection(NOTES_COLLECTION)
                .document(note.uid)
                .collection(NOTES_COLLECTION)
                .document(entity.note_id)
                .set(entity)
                .addOnSuccessListener {
                    Log.d("insertStatus", "insertSuccess")
                }
                .addOnFailureListener {
                    
                    Log.d("insertStatus", "insertFail")
                }
                .await()
    }

    override suspend fun deleteNote(note: Note) {

        firestore
            .collection(NOTES_COLLECTION)
            .document(note.uid)
            .collection(NOTES_COLLECTION)
            .document(note.note_id) // then what note to delete ?
            .delete()
            .addOnFailureListener {
                
            }
            .await()
    }

    override suspend fun insertDeletedNote(note: Note) {

        val entity = networkMapper.mapToEntity(note)
        firestore
            .collection(DELETES_COLLECTION)
            .document(note.uid)
            .collection(NOTES_COLLECTION)
            .document(entity.note_id)
            .set(entity)
            .addOnFailureListener {
                
            }
            .await()
    }

    override suspend fun insertDeletedNotes(notes: List<Note>) {
        if(notes.size > 500){
            throw Exception("Cannot delete more than 500 notes at a time in firestore.")
        }

        val collectionRef = firestore
            .collection(DELETES_COLLECTION)

        firestore.runBatch { batch ->
            for(note in notes){
                val documentRef = collectionRef
                    .document(note.uid)
                    .collection(NOTES_COLLECTION)
                    .document(note.note_id)
                batch.set(documentRef, networkMapper.mapToEntity(note))
            }
        }.addOnFailureListener {
            
        }.await()
    }

    override suspend fun deleteDeletedNote(note: Note) {

        val entity = networkMapper.mapToEntity(note)
        firestore
            .collection(DELETES_COLLECTION)
            .document(note.uid)
            .collection(NOTES_COLLECTION)
            .document(entity.note_id)
            .delete()
            .addOnFailureListener {
                
            }
            .await()
    }

    override suspend fun deleteAllNotes() {

        firestore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .delete()
            .await()
        firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .delete()
            .await()
    }

    override suspend fun getDeletedNotes(): List<Note> {

        val currentUserID = FirebaseAuth.getInstance().currentUser?.let {
            it.uid
        } ?: ""

        return networkMapper.entityListToNoteList(
            firestore
                .collection(DELETES_COLLECTION)
                .document(currentUserID)
                .collection(NOTES_COLLECTION)
                .get()
                .addOnFailureListener {

                }
            .await().toObjects(NoteNetworkEntity::class.java)
        )
    }

    override suspend fun searchNote(note: Note): Note? {

        return firestore
            .collection(NOTES_COLLECTION)
            .document(note.uid)
            .collection(NOTES_COLLECTION)
            .document(note.note_id)
            .get()
            .addOnFailureListener {
                
            }
            .await()
            .toObject(NoteNetworkEntity::class.java)?.let {
                networkMapper.mapFromEntity(it)
            }
    }

    override suspend fun getAllNotes(): List<Note> {

        val currentUserID = FirebaseAuth.getInstance().currentUser?.let {
            it.uid
        } ?: ""

        return networkMapper.entityListToNoteList(
            firestore
                .collection(NOTES_COLLECTION)
                .document(currentUserID)
                .collection(NOTES_COLLECTION)
                .get()
                .addOnFailureListener {

                }
                .await()
                .toObjects(NoteNetworkEntity::class.java)
        )
    }

    override suspend fun moveNotes(notes: List<Note>, folderId: String) {

        val collectionRef = firestore
            .collection(NOTES_COLLECTION)

        firestore.runBatch { batch ->
            for(note in notes){
                val entity = networkMapper.mapToEntity(note)
                entity.note_folder_id = folderId
                entity.updated_at = Timestamp.now()
                val documentRef = collectionRef
                    .document(note.uid)
                    .collection(NOTES_COLLECTION)
                    .document(note.note_id)
                batch.set(documentRef, entity)
            }
        }.addOnFailureListener {
            
        }.await()
    }

    override suspend fun insertOrUpdateNotes(notes: List<Note>) {

        if(notes.size > 500){
            throw Exception("Cannot insert more than 500 notes at a time into firestore.")
        }

        val collectionRef = firestore
            .collection(NOTES_COLLECTION)

        firestore.runBatch { batch ->
            for(note in notes){
                val entity = networkMapper.mapToEntity(note)
                entity.updated_at = Timestamp.now()
                val documentRef = collectionRef
                    .document(note.uid)
                    .collection(NOTES_COLLECTION)
                    .document(note.note_id)
                batch.set(documentRef, entity)
            }
        }.addOnFailureListener {
            
        }.await()

    }

    companion object {
        const val NOTES_COLLECTION = "notes"
        const val DELETES_COLLECTION = "deletes"
        const val USER_ID = "c6QYqFEc11dB1DzqgmpjuKlE9xv2"
    }


}












