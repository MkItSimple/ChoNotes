package cho.chonotes.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Note(
    val note_id: String,
    val title: String,
    val body: String,
    val note_folder_id: String,
    val uid: String,
    val updated_at: String,
    val created_at: String
) : Parcelable{

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Note

        if (note_id != other.note_id) return false
        if (title != other.title) return false
        if (body != other.body) return false
        if (created_at != other.created_at) return false

        return true
    }

    override fun hashCode(): Int {
        var result = note_id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + created_at.hashCode()
        return result
    }
}