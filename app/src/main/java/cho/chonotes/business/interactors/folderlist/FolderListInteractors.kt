package cho.chonotes.business.interactors.folderlist

import cho.chonotes.business.interactors.common.DeleteFolder
import cho.chonotes.framework.presentation.folderlist.state.FolderListViewState


// Use cases
class FolderListInteractors (
    val renameFolder: RenameFolder,
    val insertDefaultFolder: InsertDefaultFolder,
    val insertNewFolder: InsertNewFolder,
    val deleteFolder: DeleteFolder<FolderListViewState>,
    val searchFolders: SearchFolders,
    val searchFoldersWithNotes: SearchFoldersWithNotes,
    val getNumFolders: GetNumFolders,
    val restoreDeletedFolder: RestoreDeletedFolder,
    val deleteMultipleFolders: DeleteMultipleFolders,
    val deleteMultipleFoldersAndNotes: DeleteMultipleFoldersAndNotes,
    val insertMultipleFolders: InsertMultipleFolders // for testing
)














