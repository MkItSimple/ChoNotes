package cho.chonotes.framework.presentation.notelist.state

import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.state.StateEvent
import cho.chonotes.business.domain.state.StateMessage

sealed class NoteListStateEvent: StateEvent {

    class InsertNewNoteEvent(
        val title: String,
        val toFolder: String
    ): NoteListStateEvent() {

        override fun errorInfo(): String {
            return "Error inserting new note."
        }

        override fun eventName(): String {
            return "InsertNewNoteEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class InsertMultipleNotesEvent(
        val numNotes: Int
    ): NoteListStateEvent() {

        override fun errorInfo(): String {
            return "Error inserting the notes."
        }

        override fun eventName(): String {
            return "InsertMultipleNotesEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class DeleteNoteEvent(
        val note: Note
    ): NoteListStateEvent(){

        override fun errorInfo(): String {
            return "Error deleting note."
        }

        override fun eventName(): String {
            return "DeleteNoteEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class DeleteMultipleNotesEvent(
        val notes: List<Note>
    ): NoteListStateEvent(){

        override fun errorInfo(): String {
            return "Error deleting the selected notes."
        }

        override fun eventName(): String {
            return "DeleteMultipleNotesEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class RestoreDeletedNoteEvent(
        val note: Note
    ): NoteListStateEvent() {

        override fun errorInfo(): String {
            return "Error restoring the note that was deleted."
        }

        override fun eventName(): String {
            return "RestoreDeletedNoteEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

    class SearchNotesEvent(
        val clearLayoutManagerState: Boolean = true
    ): NoteListStateEvent(){

        override fun errorInfo(): String {
            return "Error getting list of notes."
        }

        override fun eventName(): String {
            return "SearchNotesEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class GetNumNotesInCacheEvent: NoteListStateEvent(){

        override fun errorInfo(): String {
            return "Error getting the number of notes from the cache."
        }

        override fun eventName(): String {
            return "GetNumNotesInCacheEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class CreateStateMessageEvent(
        val stateMessage: StateMessage
    ): NoteListStateEvent(){

        override fun errorInfo(): String {
            return "Error creating a new state message."
        }

        override fun eventName(): String {
            return "CreateStateMessageEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

    class MoveNotesEvent(
        val selectedNotes: List<Note>,
        val folderId: String
    ): NoteListStateEvent(){

        override fun errorInfo(): String {
            return "Error deleting note."
        }

        override fun eventName(): String {
            return "MoveNotesEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }
}




















