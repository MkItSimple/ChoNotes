package cho.chonotes.business.interactors.notelist

import cho.chonotes.business.interactors.common.DeleteNote
import cho.chonotes.framework.presentation.notelist.state.NoteListViewState

class NoteListInteractors (
    val insertNewNote: InsertNewNote,
    val deleteNote: DeleteNote<NoteListViewState>,
    val searchNotes: SearchNotes,
    val getNumNotes: GetNumNotes,
    val restoreDeletedNote: RestoreDeletedNote,
    val deleteMultipleNotes: DeleteMultipleNotes,
    val insertMultipleNotes: InsertMultipleNotes,
    val moveNotes: MoveNotes
)














