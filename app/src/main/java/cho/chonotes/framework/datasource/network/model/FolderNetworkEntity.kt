package cho.chonotes.framework.datasource.network.model

import com.google.firebase.Timestamp


data class FolderNetworkEntity(

    var folder_id: String,

    var folder_name: String,

    var notes_count: Int,

    var uid: String,

    var updated_at: Timestamp,

    var created_at: Timestamp

){

    // no arg constructor for firestore
    constructor(): this(
        "",
        "",
        0,
        "",
        Timestamp.now(),
        Timestamp.now()
    )



    companion object{

        const val UPDATED_AT_FIELD = "updated_at"
        const val FOLDER_NAME_FIELD = "folder_name"
        const val NOTES_COUNT_FIELD = "notes_count"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FolderNetworkEntity

        if (folder_id != other.folder_id) return false
        if (folder_name != other.folder_name) return false
        if (uid != other.uid) return false
//        if (updated_at != other.updated_at) return false // ignore
        if (created_at != other.created_at) return false

        return true
    }

    override fun hashCode(): Int {
        var result = folder_id.hashCode()
        result = 31 * result + folder_name.hashCode()
        result = 31 * result + uid.hashCode()
        result = 31 * result + updated_at.hashCode()
        result = 31 * result + created_at.hashCode()
        return result
    }
}









