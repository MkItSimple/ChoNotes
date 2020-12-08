package cho.chonotes.framework.presentation.folderlist.state

sealed class FolderListToolbarState {

    class MultiSelectionState: FolderListToolbarState(){

        override fun toString(): String {
            return "MultiSelectionState"
        }
    }

    class SearchViewState: FolderListToolbarState(){

        override fun toString(): String {
            return "SearchViewState"
        }
    }
}