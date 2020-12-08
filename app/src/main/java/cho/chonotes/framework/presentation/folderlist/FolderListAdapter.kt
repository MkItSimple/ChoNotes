package cho.chonotes.framework.presentation.folderlist

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

class FolderListAdapter(
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
                R.layout.layout_folder_list_item,
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

        private val COLOR_GREY = R.color.blue_lighten
        private val COLOR_PRIMARY = R.color.colorPrimary
        private lateinit var folder: Folder
        private lateinit var notesCount: String
        private lateinit var notesString: String


        fun bind(item: Folder) = with(itemView) {

            // all folders are clickable exept notes folder
            setOnClickListener {
                interaction?.onItemSelected(adapterPosition, folder)
            }

            if (item.folder_id != "notes") {
                setOnLongClickListener {
                    interaction?.activateMultiSelectionMode()
                    interaction?.onItemSelected(adapterPosition, folder)
                    true
                }
            }

            folder = item
            folder_name.text = item.folder_name.capitalize()
//            folder_timestamp.text = dateUtil.removeTimeFromDateString(item.updated_at)
            notesCount = item.notes_count.toString()
            notesString = when(item.notes_count > 1) {
                true -> "$notesCount notes - ${item.folder_id.take(7)}"
                false -> "$notesCount note - ${item.folder_id.take(7)}"
            }

            folder_timestamp.text = notesString

            selectedFolders.observe(lifecycleOwner, Observer { folders ->

                if(folders != null){
                    if(folders.contains(folder)){
                        changeColor(
                            newColor = COLOR_GREY
                        )
                    }
                    else{
                        changeColor(
                            newColor = COLOR_PRIMARY
                        )
                    }
                }else{
                    changeColor(
                        newColor = COLOR_PRIMARY
                    )
                }
            })
        }
    }

    interface Interaction {

        fun onItemSelected(position: Int, item: Folder)

        fun restoreListPosition()

        fun isMultiSelectionModeEnabled(): Boolean

        fun activateMultiSelectionMode()

        fun isFolderSelected(folder: Folder): Boolean
    }
}