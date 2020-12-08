package cho.chonotes.framework.presentation.folderlist.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.framework.presentation.folderlist.state.FolderListToolbarState.*

class FolderListInteractionManager {
    private val _selectedFolders: MutableLiveData<ArrayList<Folder>> = MutableLiveData()

    private val _toolbarState: MutableLiveData<FolderListToolbarState>
            = MutableLiveData(SearchViewState())

    val selectedFolders: LiveData<ArrayList<Folder>>
        get() = _selectedFolders

    val toolbarState: LiveData<FolderListToolbarState>
        get() = _toolbarState

    fun setToolbarState(state: FolderListToolbarState){
        _toolbarState.value = state
    }

    fun getSelectedFolders():ArrayList<Folder> = _selectedFolders.value?: ArrayList()

    fun isMultiSelectionStateActive(): Boolean{
        return _toolbarState.value.toString() == MultiSelectionState().toString()
    }

    fun addOrRemoveFolderFromSelectedList(folder: Folder){
        var list = _selectedFolders.value
        if(list == null){
            list = ArrayList()
        }
        if (list.contains(folder)){
            list.remove(folder)
        }
        else{
            list.add(folder)
        }
        _selectedFolders.value = list
    }

    fun isFolderSelected(folder: Folder): Boolean{
        return _selectedFolders.value?.contains(folder)?: false
    }

    fun clearSelectedFolders(){
        _selectedFolders.value = null
    }
}