package cho.chonotes.framework.datasource.network.model

import com.google.firebase.Timestamp


data class NoteNetworkEntity(

    var note_id: String,

    var title: String,

    var body: String,

    var note_folder_id: String,

    var uid: String,

    var updated_at: Timestamp,

    var created_at: Timestamp

){
    constructor(): this(
        "",
        "",
        "",
        "",
        "",
        Timestamp.now(),
        Timestamp.now()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NoteNetworkEntity

        if (note_id != other.note_id) return false
        if (title != other.title) return false
        if (body != other.body) return false
        if (note_folder_id != other.note_folder_id) return false
        if (uid != other.uid) return false
        if (created_at != other.created_at) return false

        return true
    }

    override fun hashCode(): Int {
        var result = note_id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + note_folder_id.hashCode()
        result = 31 * result + uid.hashCode()
        result = 31 * result + updated_at.hashCode()
        result = 31 * result + created_at.hashCode()
        return result
    }
}









