package cho.chonotes.framework.datasource.cache.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteCacheEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "note_id")
    var note_id: String,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "body")
    var body: String,

    @ColumnInfo(name = "note_folder_id")
    var note_folder_id: String,

    @ColumnInfo(name = "uid")
    var uid: String,

    @ColumnInfo(name = "updated_at")
    var updated_at: String,

    @ColumnInfo(name = "created_at")
    var created_at: String

){

    companion object{

        fun nullTitleError(): String{
            return "You must enter a title."
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NoteCacheEntity

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



