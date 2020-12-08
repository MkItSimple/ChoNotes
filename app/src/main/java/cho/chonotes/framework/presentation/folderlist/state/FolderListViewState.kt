package cho.chonotes.framework.presentation.folderlist.state

import android.os.Parcelable
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.state.ViewState
import cho.chonotes.framework.datasource.cache.model.FolderWithNotesCacheEntity
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class FolderListViewState(

    var folder: Folder? = null,
    var isUpdatePending: Boolean? = null,

    var folderList: ArrayList<Folder>? = null,
    var newFolder: Folder? = null, // folder that can be created with fab
    var folderPendingDelete: FolderPendingDelete? = null, // set when delete is pending (can be undone)
    var searchQuery: String? = null,
    var page: Int? = null,
    var isQueryExhausted: Boolean? = null,
    var filter: String? = null,
    var order: String? = null,
    var layoutManagerState: Parcelable? = null,
    var numFoldersInCache: Int? = null,

    var folderWithNotesCacheEntityList : ArrayList<Folder>? = null
//    var folderWithNotesList : ArrayList<Folder>? = null

    ) : Parcelable, ViewState {

    @Parcelize
    data class FolderPendingDelete(
        var folder: Folder? = null,
        var listPosition: Int? = null
    ) : Parcelable
}