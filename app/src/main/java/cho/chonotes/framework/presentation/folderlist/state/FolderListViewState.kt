package cho.chonotes.framework.presentation.folderlist.state

import android.os.Parcelable
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FolderListViewState(

    var folder: Folder? = null,
    var isUpdatePending: Boolean? = null,

    var folderList: ArrayList<Folder>? = null,
    var newFolder: Folder? = null,
    var folderPendingDelete: FolderPendingDelete? = null,
    var searchQuery: String? = null,
    var page: Int? = null,
    var isQueryExhausted: Boolean? = null,
    var filter: String? = null,
    var order: String? = null,
    var layoutManagerState: Parcelable? = null,
    var numFoldersInCache: Int? = null,

    var folderWithNotesCacheEntityList : ArrayList<Folder>? = null

    ) : Parcelable, ViewState {

    @Parcelize
    data class FolderPendingDelete(
        var folder: Folder? = null,
        var listPosition: Int? = null
    ) : Parcelable
}