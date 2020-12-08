package cho.chonotes.framework.presentation.folderlist.state

import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.state.StateEvent
import cho.chonotes.business.domain.state.StateMessage


sealed class FolderListStateEvent: StateEvent {

    class RenameFolderEvent(
        val selectedFolder: Folder,
        val newFolderName: String
    ) : FolderListStateEvent() {

        override fun errorInfo(): String {
            return "Error renaming folder."
        }

        override fun eventName(): String {
            return "RenameFolderEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class InsertDefaultFolderEvent : FolderListStateEvent() {

        override fun errorInfo(): String {
            return "Error inserting default folder."
        }

        override fun eventName(): String {
            return "InsertDefaultFolderEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class InsertNewFolderEvent(
        val folder_name: String
    ): FolderListStateEvent() {

        override fun errorInfo(): String {
            return "Error inserting new folder."
        }

        override fun eventName(): String {
            return "InsertNewFolderEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    // for testing
    class InsertMultipleFoldersEvent(
        val numFolders: Int
    ): FolderListStateEvent() {

        override fun errorInfo(): String {
            return "Error inserting the folders."
        }

        override fun eventName(): String {
            return "InsertMultipleFoldersEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class DeleteFolderEvent(
        val folder: Folder
    ): FolderListStateEvent(){

        override fun errorInfo(): String {
            return "Error deleting folder."
        }

        override fun eventName(): String {
            return "DeleteFolderEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class DeleteMultipleFoldersEvent(
        val folders: List<Folder>
    ): FolderListStateEvent(){

        override fun errorInfo(): String {
            return "Error deleting the selected folders."
        }

        override fun eventName(): String {
            return "DeleteMultipleFoldersEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class DeleteMultipleFoldersAndNotesEvent(
        val folders: List<Folder>
    ): FolderListStateEvent(){

        override fun errorInfo(): String {
            return "Error deleting the selected folders."
        }

        override fun eventName(): String {
            return "DeleteMultipleFoldersAndNotesEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class RestoreDeletedFolderEvent(
        val folder: Folder
    ): FolderListStateEvent() {

        override fun errorInfo(): String {
            return "Error restoring the folder that was deleted."
        }

        override fun eventName(): String {
            return "RestoreDeletedFolderEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

    class SearchFoldersEvent(
        val clearLayoutManagerState: Boolean = true
    ): FolderListStateEvent(){

        override fun errorInfo(): String {
            return "Error getting list of folders."
        }

        override fun eventName(): String {
            return "SearchFoldersEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class SearchFoldersWithNotesEvent(
        val clearLayoutManagerState: Boolean = true
    ): FolderListStateEvent(){

        override fun errorInfo(): String {
            return "Error getting list of folders with notes."
        }

        override fun eventName(): String {
            return "SearchFoldersWithNotesEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class GetNumFoldersInCacheEvent: FolderListStateEvent(){

        override fun errorInfo(): String {
            return "Error getting the number of folders from the cache."
        }

        override fun eventName(): String {
            return "GetNumFoldersInCacheEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class CreateStateMessageEvent(
        val stateMessage: StateMessage
    ): FolderListStateEvent(){

        override fun errorInfo(): String {
            return "Error creating a new state message."
        }

        override fun eventName(): String {
            return "CreateStateMessageEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

}