package cho.chonotes.framework.datasource.cache.model

import androidx.room.Embedded
import androidx.room.Relation

data class FolderWithNotesCacheEntity (
    @Embedded
    val folderCacheEntity: FolderCacheEntity,
    @Relation(
        parentColumn = "folder_id",
        entityColumn = "note_folder_id"
    )
    val notes: List<NoteCacheEntity>
)