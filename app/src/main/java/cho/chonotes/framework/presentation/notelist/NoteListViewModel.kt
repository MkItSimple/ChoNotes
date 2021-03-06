package cho.chonotes.framework.presentation.notelist

import android.content.SharedPreferences
import android.os.Parcelable
import androidx.lifecycle.LiveData
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.model.NoteFactory
import cho.chonotes.business.domain.state.*
import cho.chonotes.business.interactors.notelist.DeleteMultipleNotes.Companion.DELETE_NOTES_YOU_MUST_SELECT
import cho.chonotes.business.interactors.notelist.NoteListInteractors
import cho.chonotes.framework.datasource.cache.database.NOTE_FILTER_DATE_CREATED
import cho.chonotes.framework.datasource.cache.database.NOTE_ORDER_DESC
import cho.chonotes.framework.datasource.preferences.PreferenceKeys.Companion.NOTE_FILTER
import cho.chonotes.framework.datasource.preferences.PreferenceKeys.Companion.NOTE_ORDER
import cho.chonotes.framework.presentation.common.BaseViewModel
import cho.chonotes.framework.presentation.notelist.state.NoteListInteractionManager
import cho.chonotes.framework.presentation.notelist.state.NoteListStateEvent.*
import cho.chonotes.framework.presentation.notelist.state.NoteListToolbarState
import cho.chonotes.framework.presentation.notelist.state.NoteListViewState
import cho.chonotes.framework.presentation.notelist.state.NoteListViewState.NotePendingDelete
import cho.chonotes.util.printLogD
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


const val DELETE_PENDING_ERROR = "There is already a pending delete operation."
const val NOTE_PENDING_DELETE_BUNDLE_KEY = "pending_delete"

const val NOTE_LIST_ERROR_RETRIEVEING_SELECTED_FOLDER = "Error retrieving selected folder from bundle."
const val NOTE_LIST_SELECTED_FOLDER_BUNDLE_KEY = "selectedFolder"
const val NOTE_LIST_SELECTED_NOTES_BUNDLE_KEY = "selectedNotes"

const val NOTE_LIST_PREVIOUS_FRAGMENT_BUNDLE_KEY = "previousFragment"
const val NOTES = ""

@ExperimentalCoroutinesApi
@FlowPreview
@Singleton
class NoteListViewModel
@Inject
constructor(
    private val noteInteractors: NoteListInteractors,
    private val noteFactory: NoteFactory,
    private val editor: SharedPreferences.Editor,
    sharedPreferences: SharedPreferences
): BaseViewModel<NoteListViewState>(){

    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val noteListInteractionManager =
        NoteListInteractionManager()

    val toolbarState: LiveData<NoteListToolbarState>
            get() = noteListInteractionManager.toolbarState

    init {
        setNoteFilter(
            sharedPreferences.getString(
                NOTE_FILTER,
                NOTE_FILTER_DATE_CREATED
            )
        )
        setNoteOrder(
            sharedPreferences.getString(
                NOTE_ORDER,
                NOTE_ORDER_DESC
            )
        )
    }

    override fun handleNewData(data: NoteListViewState) {

        data.let { viewState ->
            viewState.noteList?.let { noteList ->
                setNoteListData(noteList)
            }

            viewState.numNotesInCache?.let { numNotes ->
                setNumNotesInCache(numNotes)
            }

            viewState.newNote?.let { note ->
                setNote(note)
            }

            viewState.notePendingDelete?.let { restoredNote ->
                restoredNote.note?.let { note ->
                    setRestoredNoteId(note)
                }
                setNotePendingDelete(null)
            }
        }

    }

    override fun setStateEvent(stateEvent: StateEvent) {

        val job: Flow<DataState<NoteListViewState>?> = when(stateEvent){

            is InsertNewNoteEvent -> {
                noteInteractors.insertNewNote.insertNewNote(
                    title = stateEvent.title,
                    toFolder = stateEvent.toFolder,
                    uid = uid,
                    stateEvent = stateEvent
                )
            }

            is InsertMultipleNotesEvent -> {
                noteInteractors.insertMultipleNotes.insertNotes(
                    numNotes = stateEvent.numNotes,
                    stateEvent = stateEvent
                )
            }

            is DeleteNoteEvent -> {
                noteInteractors.deleteNote.deleteNote(
                    note = stateEvent.note,
                    stateEvent = stateEvent
                )
            }

            is DeleteMultipleNotesEvent -> {
                noteInteractors.deleteMultipleNotes.deleteNotes(
                    notes = stateEvent.notes,
                    stateEvent = stateEvent
                )
            }

            is MoveNotesEvent -> {
                noteInteractors.moveNotes.moveNotes(
                    selectedNotes = stateEvent.selectedNotes,
                    folderId = stateEvent.folderId,
                    stateEvent = stateEvent
                )
            }


            is RestoreDeletedNoteEvent -> {
                noteInteractors.restoreDeletedNote.restoreDeletedNote(
                    note = stateEvent.note,
                    stateEvent = stateEvent
                )
            }

            is SearchNotesEvent -> {
                if(stateEvent.clearLayoutManagerState){
                    clearLayoutManagerState()
                }
                noteInteractors.searchNotes.searchNotes(
                    uid = uid,
                    query = getSearchQuery(),
                    folderId = getSelectedFolderId(),
                    filterAndOrder = getOrder() + getFilter(),
                    page = getPage(),
                    stateEvent = stateEvent
                )
            }

            is GetNumNotesInCacheEvent -> {
                noteInteractors.getNumNotes.getNumNotes(
                    stateEvent = stateEvent
                )
            }

            is CreateStateMessageEvent -> {
                emitStateMessageEvent(
                    stateMessage = stateEvent.stateMessage,
                    stateEvent = stateEvent
                )
            }

            else -> {
                emitInvalidStateEvent(stateEvent)
            }
        }
        launchJob(stateEvent, job)
    }

    private fun removeSelectedNotesFromList(){
        val update = getCurrentViewStateOrNew()
        update.noteList?.removeAll(getSelectedNotes())
        setViewState(update)
        clearSelectedNotes()
    }

    fun deleteNotes(){
        if(getSelectedNotes().size > 0){
            setStateEvent(DeleteMultipleNotesEvent(getSelectedNotes()))
            removeSelectedNotesFromList()
        }
        else{
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = DELETE_NOTES_YOU_MUST_SELECT,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Info()
                        )
                    )
                )
            )
        }
    }

    fun getSelectedNotes() = noteListInteractionManager.getSelectedNotes()

    fun setToolbarState(state: NoteListToolbarState)
            = noteListInteractionManager.setToolbarState(state)

    fun isMultiSelectionStateActive()
            = noteListInteractionManager.isMultiSelectionStateActive()

    override fun initNewViewState(): NoteListViewState {
        return NoteListViewState()
    }

    fun getFilter(): String {
        return getCurrentViewStateOrNew().filter
            ?: NOTE_FILTER_DATE_CREATED
    }

    fun getOrder(): String {
        return getCurrentViewStateOrNew().order
            ?: NOTE_ORDER_DESC
    }

    private fun getSearchQuery(): String {
        return getCurrentViewStateOrNew().searchQuery
            ?: return ""
    }

    fun getSelectedFolderId(): String {
        return getCurrentViewStateOrNew().selectedFolderId
            ?: return NOTES
    }

    private fun getPage(): Int{
        return getCurrentViewStateOrNew().page
            ?: return 1
    }

    private fun setNoteListData(notesList: ArrayList<Note>){
        val update = getCurrentViewStateOrNew()
        update.noteList = notesList
        setViewState(update)
    }

    fun setQueryExhausted(isExhausted: Boolean){
        val update = getCurrentViewStateOrNew()
        update.isQueryExhausted = isExhausted
        setViewState(update)
    }

    fun setNote(note: Note?){
        val update = getCurrentViewStateOrNew()
        update.newNote = note
        setViewState(update)
    }

    fun setQuery(query: String?){
        val update =  getCurrentViewStateOrNew()
        update.searchQuery = query
        setViewState(update)
    }

    private fun setRestoredNoteId(restoredNote: Note){
        val update = getCurrentViewStateOrNew()
        update.noteList?.let { noteList ->
            for((index, note) in noteList.withIndex()){
                if(note.title == restoredNote.title){
                    noteList.remove(note)
                    noteList.add(index, restoredNote)
                    update.noteList = noteList
                    break
                }
            }
        }
        setViewState(update)
    }

    fun isDeletePending(): Boolean{
        val pendingNote = getCurrentViewStateOrNew().notePendingDelete
        if(pendingNote != null){
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = DELETE_PENDING_ERROR,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Info()
                        )
                    )
                )
            )
            return true
        }
        else{
            return false
        }
    }

    fun beginPendingDelete(note: Note){
        setNotePendingDelete(note)
        removePendingNoteFromList(note)
        setStateEvent(
            DeleteNoteEvent(
                note = note
            )
        )
    }

    private fun removePendingNoteFromList(note: Note?){
        val update = getCurrentViewStateOrNew()
        val list = update.noteList
        if(list?.contains(note) == true){
            list.remove(note)
            update.noteList = list
            setViewState(update)
        }
    }

    fun undoDelete(){

        val update = getCurrentViewStateOrNew()
        update.notePendingDelete?.let { note ->

            if(note.listPosition != null && note.note != null){
                update.noteList?.add(
                    note.listPosition as Int,
                    note.note as Note
                )
                setStateEvent(RestoreDeletedNoteEvent(note.note as Note))
            }
        }
        setViewState(update)
    }


    fun setNotePendingDelete(note: Note?){
        val update = getCurrentViewStateOrNew()
        if(note != null){
            update.notePendingDelete = NotePendingDelete(
                note = note,
                listPosition = findListPositionOfNote(note)
            )
        }
        else{
            update.notePendingDelete = null
        }
        setViewState(update)
    }

    private fun findListPositionOfNote(note: Note?): Int {
        val viewState = getCurrentViewStateOrNew()
        viewState.noteList?.let { noteList ->
            for((index, item) in noteList.withIndex()){
                if(item.note_id == note?.note_id){
                    return index
                }
            }
        }
        return 0
    }

    private fun setNumNotesInCache(numNotes: Int){
        val update = getCurrentViewStateOrNew()
        update.numNotesInCache = numNotes
        setViewState(update)
    }

    fun createNewNote(
        id: String? = null,
        title: String,
        body: String? = null
    ) = noteFactory.createSingleNote(id, title, body, note_folder_id = "", uid = "")

    private fun getNoteListSize() = getCurrentViewStateOrNew().noteList?.size?: 0

    private fun getNumNotesInCache() = getCurrentViewStateOrNew().numNotesInCache?: 0

    fun isPaginationExhausted() = getNoteListSize() >= getNumNotesInCache()

    private fun resetPage(){
        val update = getCurrentViewStateOrNew()
        update.page = 1
        setViewState(update)
    }

    fun getActiveJobs() = dataChannelManager.getActiveJobs()

    fun isQueryExhausted(): Boolean{
        printLogD("NoteListViewModel",
            "is query exhasuted? ${getCurrentViewStateOrNew().isQueryExhausted?: true}")
        return getCurrentViewStateOrNew().isQueryExhausted?: true
    }

    fun clearList(){
        printLogD("ListViewModel", "clearList")
        val update = getCurrentViewStateOrNew()
        update.noteList = ArrayList()
        setViewState(update)
    }

    // workaround for tests
    // can't submit an empty string because SearchViews SUCK
    fun clearSearchQuery(){
        setQuery("")
        clearList()
        loadFirstPage()
    }

    fun loadFirstPage() {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(SearchNotesEvent())
        printLogD("NoteListViewModel",
            "loadFirstPage: ${getCurrentViewStateOrNew().searchQuery}")
    }

    fun nextPage(){
        if(!isQueryExhausted()){
            printLogD("NoteListViewModel", "attempting to load next page...")
            clearLayoutManagerState()
            incrementPageNumber()
            setStateEvent(SearchNotesEvent())
        }
    }

    private fun incrementPageNumber(){
        val update = getCurrentViewStateOrNew()
        val page = update.copy().page ?: 1
        update.page = page.plus(1)
        setViewState(update)
    }

    fun retrieveNumNotesInCache(){
        setStateEvent(GetNumNotesInCacheEvent())
    }

    fun refreshSearchQuery(){
        setQueryExhausted(false)
        setStateEvent(SearchNotesEvent(false))
    }

    fun getLayoutManagerState(): Parcelable? {
        return getCurrentViewStateOrNew().layoutManagerState
    }

    fun setLayoutManagerState(layoutManagerState: Parcelable){
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = layoutManagerState
        setViewState(update)
    }

    private fun clearLayoutManagerState(){
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = null
        setViewState(update)
    }

    fun addOrRemoveNoteFromSelectedList(note: Note)
            = noteListInteractionManager.addOrRemoveNoteFromSelectedList(note)

    fun isNoteSelected(note: Note): Boolean
            = noteListInteractionManager.isNoteSelected(note)

    fun clearSelectedNotes() = noteListInteractionManager.clearSelectedNotes()

    fun setNoteFilter(filter: String?){
        filter?.let{
            val update = getCurrentViewStateOrNew()
            update.filter = filter
            setViewState(update)
        }
    }

    fun setNoteOrder(order: String?){
        val update = getCurrentViewStateOrNew()
        update.order = order
        setViewState(update)
    }

    fun saveFilterOptions(filter: String, order: String){
        editor.putString(NOTE_FILTER, filter)
        editor.apply()

        editor.putString(NOTE_ORDER, order)
        editor.apply()
    }

    fun setSelectedFolderId(selectedFolderId: String) {
        val update = getCurrentViewStateOrNew()
        update.selectedFolderId = selectedFolderId
        setViewState(update)
    }
}












































