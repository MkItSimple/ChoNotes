package cho.chonotes.business.interactors.notedetail

import cho.chonotes.business.interactors.common.DeleteNote
import cho.chonotes.framework.presentation.notedetail.state.NoteDetailViewState

// Use cases
class NoteDetailInteractors (
    val deleteNote: DeleteNote<NoteDetailViewState>,
    val updateNote: UpdateNote
)














