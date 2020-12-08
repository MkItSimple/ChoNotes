package cho.chonotes.framework.presentation.selectfolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import androidx.recyclerview.widget.AsyncListDiffer
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import cho.chonotes.R
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.business.domain.util.DateUtil
import cho.chonotes.framework.presentation.common.changeColor
import cho.chonotes.util.printLogD
import kotlinx.android.synthetic.main.layout_folder_list_item.view.*
import kotlinx.android.synthetic.main.layout_folder_list_item.view.folder_name
import kotlinx.android.synthetic.main.layout_select_folder_item.view.*

class SelectFolderAdapter(
    private val interaction: Interaction? = null,
    private val lifecycleOwner: LifecycleOwner,
    private val selectedFolders: LiveData<ArrayList<Folder>>,
    private val dateUtil: DateUtil
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Folder>() {

        override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem.folder_id == newItem.folder_id
        }

        override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_select_folder_item,
                parent,
                false
            ),
            interaction,
            lifecycleOwner,
            selectedFolders,
            dateUtil
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FolderViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Folder>) {
        val commitCallback = Runnable {
            // if process died must restore list position
            // very annoying
            interaction?.restoreListPosition()
        }
        printLogD("listadapter", "size: ${list.size}")
        differ.submitList(list, commitCallback)
    }

    fun getFolder(index: Int): Folder? {
        return try{
            differ.currentList[index]
        }catch (e: IndexOutOfBoundsException){
            e.printStackTrace()
            null
        }
    }

    class FolderViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        private val lifecycleOwner: LifecycleOwner,
        private val selectedFolders: LiveData<ArrayList<Folder>>,
        private val dateUtil: DateUtil
    ) : RecyclerView.ViewHolder(itemView) {

        private val COLOR_GREY = R.color.app_background_color
        private val COLOR_GREY_5 = R.color.blue_grey5
        private val COLOR_PRIMARY = R.color.colorPrimary
        private lateinit var folder: Folder

        fun bind(item: Folder) = with(itemView) {
            setOnClickListener {
                interaction?.onItemSelected(adapterPosition, folder)
            }

            folder = item
            folder_name.text = item.folder_name
            notes_count.text = item.notes_count.toString()
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: Folder)
        fun restoreListPosition()
    }
}