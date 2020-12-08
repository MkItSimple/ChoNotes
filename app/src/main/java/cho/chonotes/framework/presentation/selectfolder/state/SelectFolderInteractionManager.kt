package cho.chonotes.framework.presentation.selectfolder.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cho.chonotes.business.domain.model.Folder

class SelectFolderInteractionManager {
    private val _selectedFolder: MutableLiveData<Folder> = MutableLiveData()

    val selectedFolder: LiveData<Folder>
        get() = _selectedFolder

    fun getSelectedFolder(): Folder = _selectedFolder.value!!

    fun clearSelectedFolder(){
        _selectedFolder.value = null
    }
}