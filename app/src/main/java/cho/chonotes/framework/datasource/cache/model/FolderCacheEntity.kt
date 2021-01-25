package cho.chonotes.framework.datasource.cache.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderCacheEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "folder_id")
    var folder_id: String,

    @ColumnInfo(name = "folder_name")
    var folder_name: String,

    @ColumnInfo(name = "notes_count")
    var notes_count: Int,

    @ColumnInfo(name = "uid")
    var uid: String,

    @ColumnInfo(name = "updated_at")
    var updated_at: String,

    @ColumnInfo(name = "created_at")
    var created_at: String

){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FolderCacheEntity

        if (folder_id != other.folder_id) return false
        if (folder_name != other.folder_name) return false
        if (uid != other.uid) return false
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



