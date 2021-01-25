package cho.chonotes.di

import cho.chonotes.business.data.NoteDataFactory
import cho.chonotes.business.data.cache.FakeNoteCacheDataSourceImpl
import cho.chonotes.business.data.cache.abstraction.NoteCacheDataSource
import cho.chonotes.business.data.network.FakeNoteNetworkDataSourceImpl
import cho.chonotes.business.data.network.abstraction.NoteNetworkDataSource
import cho.chonotes.business.domain.model.NoteFactory
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class DependencyContainer {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
    val dateUtil =
        DateUtil(dateFormat)
    lateinit var noteNetworkDataSource: NoteNetworkDataSource
    lateinit var noteCacheDataSource: NoteCacheDataSource
    lateinit var noteFactory: NoteFactory
    lateinit var noteDataFactory: NoteDataFactory

    init {
        isUnitTest = true // for Logger.kt
    }

    fun build() {
        this.javaClass.classLoader?.let { classLoader ->
            noteDataFactory = NoteDataFactory(classLoader)
        }
        noteFactory = NoteFactory(dateUtil)
        noteNetworkDataSource = FakeNoteNetworkDataSourceImpl(
            notesData = noteDataFactory.produceHashMapOfNotes(
                noteDataFactory.produceListOfNotes()
            ),
            deletedNotesData = HashMap(),
            dateUtil = dateUtil
        )
        noteCacheDataSource = FakeNoteCacheDataSourceImpl(
            notesData = noteDataFactory.produceHashMapOfNotes(
                noteDataFactory.produceListOfNotes()
            ),
            dateUtil = dateUtil
        )
    }

}

















