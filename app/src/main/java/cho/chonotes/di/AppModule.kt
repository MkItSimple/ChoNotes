package cho.chonotes.di

import android.content.SharedPreferences
import cho.chonotes.business.data.cache.abstraction.FolderCacheDataSource
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.cache.implementation.FolderCacheDataSourceImpl
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.NoteFactory
import cho.chonotes.business.interactors.common.DeleteNote
import cho.chonotes.business.interactors.notelist.*
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.framework.datasource.cache.database.NoteDao
import cho.chonotes.framework.datasource.cache.database.NoteDatabase
import cho.chonotes.business.data.cache.implementation.NoteCacheDataSourceImpl
import cho.chonotes.business.data.network.abstraction.FolderNetworkDataSource
import cho.chonotes.business.data.network.implementation.FolderNetworkDataSourceImpl
import cho.chonotes.framework.datasource.cache.mappers.CacheMapper
import cho.chonotes.framework.datasource.network.implementation.NoteFirestoreServiceImpl
import cho.chonotes.business.data.network.implementation.NoteNetworkDataSourceImpl
import cho.chonotes.business.domain.model.FolderFactory
import cho.chonotes.business.interactors.common.DeleteFolder
import cho.chonotes.business.interactors.folderlist.*
import cho.chonotes.business.interactors.splash.SyncDeletedNotes
import cho.chonotes.business.interactors.splash.SyncNotes
import cho.chonotes.business.interactors.notedetail.NoteDetailInteractors
import cho.chonotes.business.interactors.notedetail.UpdateNote
import cho.chonotes.business.interactors.splash.SyncDeletedFolders
import cho.chonotes.business.interactors.splash.SyncFolders
import cho.chonotes.framework.datasource.cache.abstraction.FolderDaoService
import cho.chonotes.framework.datasource.cache.abstraction.NoteDaoService
import cho.chonotes.framework.datasource.cache.database.FolderDao
import cho.chonotes.framework.datasource.cache.implementation.FolderDaoServiceImpl
import cho.chonotes.framework.datasource.cache.implementation.NoteDaoServiceImpl
import cho.chonotes.framework.datasource.cache.mappers.FolderCacheMapper
import cho.chonotes.framework.datasource.cache.mappers.FolderWithNotesCacheMapper
import cho.chonotes.framework.datasource.network.abstraction.FolderFirestoreService
import cho.chonotes.framework.datasource.network.abstraction.NoteFirestoreService
import cho.chonotes.framework.datasource.network.implementation.FolderFirestoreServiceImpl
import cho.chonotes.framework.datasource.network.mappers.FolderNetworkMapper
import cho.chonotes.framework.datasource.network.mappers.NetworkMapper
import cho.chonotes.framework.presentation.splash.NoteNetworkSyncManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object AppModule {


    // https://developer.android.com/reference/java/text/SimpleDateFormat.html?hl=pt-br
    @JvmStatic
    @Singleton
    @Provides
    fun provideDateFormat(): SimpleDateFormat {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("UTC-7") // match firestore
        return sdf
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideDateUtil(dateFormat: SimpleDateFormat): DateUtil {
        return DateUtil(
            dateFormat
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPrefsEditor(
        sharedPreferences: SharedPreferences
    ): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteFactory(dateUtil: DateUtil): NoteFactory {
        return NoteFactory(
            dateUtil
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDAO(noteDatabase: NoteDatabase): NoteDao {
        return noteDatabase.noteDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteCacheMapper(dateUtil: DateUtil): CacheMapper {
        return CacheMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkMapper(dateUtil: DateUtil): NetworkMapper {
        return NetworkMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDaoService(
        noteDao: NoteDao,
        noteEntityMapper: CacheMapper,
        dateUtil: DateUtil
    ): NoteDaoService{
        return NoteDaoServiceImpl(noteDao, noteEntityMapper, dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteCacheDataSource(
        noteDaoService: NoteDaoService
    ): NoteCacheDataSource {
        return NoteCacheDataSourceImpl(noteDaoService)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreService(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        networkMapper: NetworkMapper
    ): NoteFirestoreService {
        return NoteFirestoreServiceImpl(
            firebaseAuth,
            firebaseFirestore,
            networkMapper
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkDataSource(
        firestoreService: NoteFirestoreServiceImpl
    ): NoteNetworkDataSource {
        return NoteNetworkDataSourceImpl(
            firestoreService
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDetailInteractors(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource
    ): NoteDetailInteractors{
        return NoteDetailInteractors(
            DeleteNote(noteCacheDataSource, noteNetworkDataSource),
            UpdateNote(noteCacheDataSource, noteNetworkDataSource)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteListInteractors(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource,
        noteFactory: NoteFactory
    ): NoteListInteractors {
        return NoteListInteractors(
            InsertNewNote(noteCacheDataSource, noteNetworkDataSource, noteFactory),
            DeleteNote(noteCacheDataSource, noteNetworkDataSource),
            SearchNotes(noteCacheDataSource),
            GetNumNotes(noteCacheDataSource),
            RestoreDeletedNote(noteCacheDataSource, noteNetworkDataSource),
            DeleteMultipleNotes(noteCacheDataSource, noteNetworkDataSource),
            InsertMultipleNotes(noteCacheDataSource, noteNetworkDataSource),
            MoveNotes(noteCacheDataSource, noteNetworkDataSource)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncNotes(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource,
        dateUtil: DateUtil
    ): SyncNotes{
        return SyncNotes(
            noteCacheDataSource,
            noteNetworkDataSource,
            dateUtil

        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncDeletedNotes(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteNetworkDataSource
    ): SyncDeletedNotes{
        return SyncDeletedNotes(
            noteCacheDataSource,
            noteNetworkDataSource
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkSyncManager(
        syncFolders: SyncFolders,
        syncDeletedFolders: SyncDeletedFolders,
        syncNotes: SyncNotes,
        deletedNotes: SyncDeletedNotes
    ): NoteNetworkSyncManager {
        return NoteNetworkSyncManager(
            syncFolders,
            syncDeletedFolders,
            syncNotes,
            deletedNotes
        )
    }

    /**
    *   Folder Staff
    * */
    @JvmStatic
    @Singleton
    @Provides
    fun provideFolderFactory(dateUtil: DateUtil): FolderFactory {
        return FolderFactory(
            dateUtil
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFolderDAO(noteDatabase: NoteDatabase): FolderDao {
        return noteDatabase.folderDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFolderCacheMapper(dateUtil: DateUtil): FolderCacheMapper {
        return FolderCacheMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFolderNetworkMapper(dateUtil: DateUtil): FolderNetworkMapper {
        return FolderNetworkMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFolderDaoService(
        folderDao: FolderDao,
        folderEntityMapper: FolderCacheMapper,
        folderWithNotesEntityMapper: FolderWithNotesCacheMapper,
        dateUtil: DateUtil
    ): FolderDaoService {
        return FolderDaoServiceImpl(folderDao, folderEntityMapper, folderWithNotesEntityMapper, dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFolderCacheDataSource(
        folderDaoService: FolderDaoService
    ): FolderCacheDataSource {
        return FolderCacheDataSourceImpl(folderDaoService)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFolderFirestoreService(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        networkMapper: FolderNetworkMapper
    ): FolderFirestoreService {
        return FolderFirestoreServiceImpl(
            firebaseAuth,
            firebaseFirestore,
            networkMapper
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFolderNetworkDataSource(
        firestoreService: FolderFirestoreServiceImpl
    ): FolderNetworkDataSource {
        return FolderNetworkDataSourceImpl(
            firestoreService
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFolderListInteractors(
        folderCacheDataSource: FolderCacheDataSource,
        noteCacheDataSource: NoteCacheDataSource,
        folderNetworkDataSource: FolderNetworkDataSource,
        noteNetworkDataSource: NoteNetworkDataSource,
        folderFactory: FolderFactory
    ): FolderListInteractors {
        return FolderListInteractors(
            RenameFolder(folderCacheDataSource, folderNetworkDataSource, folderFactory),
            InsertDefaultFolder(folderCacheDataSource, folderNetworkDataSource, folderFactory),
            InsertNewFolder(folderCacheDataSource, folderNetworkDataSource, folderFactory),
            DeleteFolder(noteCacheDataSource, folderCacheDataSource, noteNetworkDataSource, folderNetworkDataSource),
            SearchFolders(folderCacheDataSource),
            SearchFoldersWithNotes(folderCacheDataSource),
            GetNumFolders(folderCacheDataSource),
            RestoreDeletedFolder(noteCacheDataSource, folderCacheDataSource, noteNetworkDataSource, folderNetworkDataSource),
            DeleteMultipleFolders(noteCacheDataSource, folderCacheDataSource, noteNetworkDataSource, folderNetworkDataSource),
            DeleteMultipleFoldersAndNotes(noteCacheDataSource, folderCacheDataSource, noteNetworkDataSource, folderNetworkDataSource),
            InsertMultipleFolders(folderCacheDataSource, folderNetworkDataSource)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncFolders(
        folderCacheDataSource: FolderCacheDataSource,
        folderNetworkDataSource: FolderNetworkDataSource,
        dateUtil: DateUtil
    ): SyncFolders{
        return SyncFolders(
            folderCacheDataSource,
            folderNetworkDataSource,
            dateUtil

        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncDeletedFolders(
        folderCacheDataSource: FolderCacheDataSource,
        folderNetworkDataSource: FolderNetworkDataSource
    ): SyncDeletedFolders{
        return SyncDeletedFolders(
            folderCacheDataSource,
            folderNetworkDataSource
        )
    }

}






















