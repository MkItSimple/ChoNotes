package cho.chonotes.framework.datasource.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import cho.chonotes.framework.datasource.cache.model.FolderCacheEntity
import cho.chonotes.framework.datasource.cache.model.NoteCacheEntity

@Database(entities = [NoteCacheEntity::class, FolderCacheEntity::class ], version = 2)
abstract class NoteDatabase: RoomDatabase() {

    abstract fun noteDao(): NoteDao

    abstract fun folderDao(): FolderDao

    companion object{
        val DATABASE_NAME: String = "note_db"
    }
}