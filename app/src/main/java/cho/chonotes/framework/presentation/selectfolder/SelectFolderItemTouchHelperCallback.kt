package cho.chonotes.framework.presentation.folderlist

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cho.chonotes.framework.presentation.folderlist.state.FolderListInteractionManager
import cho.chonotes.framework.presentation.selectfolder.SelectFolderFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class SelectFolderItemTouchHelperCallback(
    private val selectFolderItemTouchHelperAdapter: SelectFolderItemTouchHelperAdapter,
    private val folderListInteractionManager: FolderListInteractionManager
): ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(
            ItemTouchHelper.ACTION_STATE_IDLE,
            ItemTouchHelper.START or ItemTouchHelper.END
        )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        selectFolderItemTouchHelperAdapter.onItemSwiped(viewHolder.adapterPosition)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return !folderListInteractionManager.isMultiSelectionStateActive()
    }

}


interface SelectFolderItemTouchHelperAdapter{

    fun onItemSwiped(position: Int)
}















