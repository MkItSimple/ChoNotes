package cho.chonotes.framework.datasource.network.implementation

import android.util.Log
import cho.chonotes.business.domain.model.Note
import cho.chonotes.framework.datasource.network.abstraction.NoteFirestoreService
import cho.chonotes.framework.datasource.network.mappers.NetworkMapper
import cho.chonotes.framework.datasource.network.model.NoteNetworkEntity
import cho.chonotes.framework.presentation.folderlist.FOLDER_LIST_SELECTED_NOTES_BUNDLE_KEY
import cho.chonotes.util.cLog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Firestore doc refs:
 * 1. add:  https://firebase.google.com/docs/firestore/manage-data/add-data
 * 2. delete: https://firebase.google.com/docs/firestore/manage-data/delete-data
 * 3. update: https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
 * 4. query: https://firebase.google.com/docs/firestore/query-data/queries
 */
@Singleton
class NoteFirestoreServiceImpl
@Inject
constructor(
    private val firebaseAuth: FirebaseAuth, // might include auth in the future
    private val firestore: FirebaseFirestore,
    private val networkMapper: NetworkMapper
): NoteFirestoreService {

    private val currentUserID = FirebaseAuth.getInstance().currentUser?.let {
        it.uid
    } ?: ""

    override suspend fun insertOrUpdateNote(note: Note) {

            val entity = networkMapper.mapToEntity(note)
            entity.updated_at = Timestamp.now() // for updates
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
                    // send error reports to Firebase Crashlytics
                    cLog(it.message)
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
                // send error reports to Firebase Crashlytics
                cLog(it.message)
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
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
    }

    // This is for syncing
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
            // send error reports to Firebase Crashlytics
            cLog(it.message)
        }.await()
    }

    // for UNDO delete note . . to remove node from deletes and insert again to notes
    override suspend fun deleteDeletedNote(note: Note) {

        val entity = networkMapper.mapToEntity(note)
        firestore
            .collection(DELETES_COLLECTION)
            .document(note.uid)
            .collection(NOTES_COLLECTION)
            .document(entity.note_id)
            .delete()
            .addOnFailureListener {
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
    }

    // used in testing
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

    // for syncing
    override suspend fun getDeletedNotes(): List<Note> {

        return networkMapper.entityListToNoteList(
            firestore
                .collection(DELETES_COLLECTION)
                .document(currentUserID)
                .collection(NOTES_COLLECTION)
                .get()
                .addOnFailureListener {
                    // send error reports to Firebase Crashlytics
                    cLog(it.message)
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
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
            .toObject(NoteNetworkEntity::class.java)?.let {
                networkMapper.mapFromEntity(it)
            }
    }

    override suspend fun getAllNotes(): List<Note> {

        return networkMapper.entityListToNoteList(
            firestore
                .collection(NOTES_COLLECTION)
                .document(currentUserID)
                .collection(NOTES_COLLECTION)
                .get()
                .addOnFailureListener {
                    // send error reports to Firebase Crashlytics
                    cLog(it.message)
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
            // send error reports to Firebase Crashlytics
            cLog(it.message)
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
            // send error reports to Firebase Crashlytics
            cLog(it.message)
        }.await()

    }

    companion object {
        const val NOTES_COLLECTION = "notes"
        const val DELETES_COLLECTION = "deletes"
//        const val USER_ID = "9E7fDYAUTNUPFirw4R28NhBZE1u1" // hardcoded for single user
        const val USER_ID = "c6QYqFEc11dB1DzqgmpjuKlE9xv2" // hardcoded for single user
        const val EMAIL = "mitch@tabian.ca"
    }


}












