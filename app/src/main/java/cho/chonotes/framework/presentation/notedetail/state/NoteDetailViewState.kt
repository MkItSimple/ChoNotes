package cho.chonotes.framework.presentation.notedetail.state

import android.os.Parcelable
import cho.chonotes.business.domain.model.Note
import cho.chonotes.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NoteDetailViewState(

    var note: Note? = null,

    var isUpdatePending: Boolean? = null

) : Parcelable, ViewState




















