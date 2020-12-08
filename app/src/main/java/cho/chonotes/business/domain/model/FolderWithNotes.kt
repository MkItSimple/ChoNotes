package cho.chonotes.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FolderWithNotes(
    val folder_id: String,
    val folder_name: String,
    val notes: List<Note>
) : Parcelable{

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FolderWithNotes

        if (folder_id != other.folder_id) return false
        if (folder_name != other.folder_name) return false
        if (notes != other.notes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = folder_id.hashCode()
        result = 31 * result + folder_name.hashCode()
        result = 31 * result + notes.hashCode()
        return result
    }
}