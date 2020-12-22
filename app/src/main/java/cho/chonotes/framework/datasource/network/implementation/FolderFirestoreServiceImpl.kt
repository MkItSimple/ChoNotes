package cho.chonotes.framework.datasource.network.implementation

import cho.chonotes.business.domain.model.Folder
import cho.chonotes.framework.datasource.network.abstraction.FolderFirestoreService
import cho.chonotes.framework.datasource.network.mappers.FolderNetworkMapper
import cho.chonotes.framework.datasource.network.model.FolderNetworkEntity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderFirestoreServiceImpl
@Inject
constructor(
    private val firestore: FirebaseFirestore,
    private val networkMapper: FolderNetworkMapper
): FolderFirestoreService {

    override suspend fun insertOrUpdateFolder(folder: Folder) {
        val entity = networkMapper.mapToEntity(folder)
        entity.updated_at = Timestamp.now()
        firestore
            .collection(FOLDERS_COLLECTION)
            .document(entity.uid)
            .collection(FOLDERS_COLLECTION)
            .document(entity.folder_id)
            .set(entity)
            .addOnFailureListener {}
            .await()
    }

    override suspend fun deleteFolder(primaryKey: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        firestore
            .collection(FOLDERS_COLLECTION)
            .document(currentUserID)
            .collection(FOLDERS_COLLECTION)
            .document(primaryKey)
            .delete()
            .addOnFailureListener {}
            .await()
    }

    override suspend fun insertDeletedFolder(folder: Folder) {
        val entity = networkMapper.mapToEntity(folder)
        firestore
            .collection(DELETES_COLLECTION)
            .document(folder.uid)
            .collection(FOLDERS_COLLECTION)
            .document(entity.folder_id)
            .set(entity)
            .addOnFailureListener {}
            .await()
    }

    override suspend fun insertDeletedFolders(folders: List<Folder>) {
        if(folders.size > 500){
            throw Exception("Cannot delete more than 500 folders at a time in firestore.")
        }

        val collectionRef = firestore
            .collection(DELETES_COLLECTION)

        firestore.runBatch { batch ->
            for(folder in folders){
                val documentRef = collectionRef
                    .document(folder.uid)
                    .collection(FOLDERS_COLLECTION)
                    .document(folder.folder_id)
                batch.set(documentRef, networkMapper.mapToEntity(folder))
            }
        }.addOnFailureListener {}.await()
    }

    override suspend fun deleteDeletedFolder(folder: Folder) {
        val entity = networkMapper.mapToEntity(folder)
        firestore
            .collection(DELETES_COLLECTION)
            .document(folder.uid)
            .collection(FOLDERS_COLLECTION)
            .document(entity.folder_id)
            .delete()
            .addOnFailureListener {}
            .await()
    }

    override suspend fun deleteAllFolders() {
        firestore
            .collection(FOLDERS_COLLECTION)
            .document(USER_ID)
            .delete()
            .await()
        firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .delete()
            .await()
    }

    override suspend fun getDeletedFolders(): List<Folder> {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        return networkMapper.entityListToFolderList(
            firestore
                .collection(DELETES_COLLECTION)
                .document(currentUserID)
                .collection(FOLDERS_COLLECTION)
                .get()
                .addOnFailureListener {}
                .await().toObjects(FolderNetworkEntity::class.java)
        )
    }

    override suspend fun searchFolder(folder: Folder): Folder? {
        return firestore
            .collection(FOLDERS_COLLECTION)
            .document(folder.uid)
            .collection(FOLDERS_COLLECTION)
            .document(folder.folder_id)
            .get()
            .addOnFailureListener {
                
            }
            .await()
            .toObject(FolderNetworkEntity::class.java)?.let {
                networkMapper.mapFromEntity(it)
            }
    }

    override suspend fun getAllFolders(): List<Folder> {

        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        return networkMapper.entityListToFolderList(
            firestore
                .collection(FOLDERS_COLLECTION)
                .document(currentUserID)
                .collection(FOLDERS_COLLECTION)
                .get()
                .addOnFailureListener {

                }
                .await()
                .toObjects(FolderNetworkEntity::class.java)
        )
    }

    override suspend fun insertOrUpdateFolders(folders: List<Folder>) {

        if(folders.size > 500){
            throw Exception("Cannot insert more than 500 folders at a time into firestore.")
        }

        val collectionRef = firestore
            .collection(FOLDERS_COLLECTION)

        firestore.runBatch { batch ->
            for(folder in folders){
                val entity = networkMapper.mapToEntity(folder)
                entity.updated_at = Timestamp.now()
                val documentRef = collectionRef
                    .document(folder.uid)
                    .collection(FOLDERS_COLLECTION)
                    .document(folder.folder_id)
                batch.set(documentRef, entity)
            }
        }.addOnFailureListener {
            
        }.await()

    }

    companion object {
        const val FOLDERS_COLLECTION = "folders"
        const val DELETES_COLLECTION = "deletes"
        const val USER_ID = "9E7fDYAUTNUPFirw4R28NhBZE1u1"
    }
}












