package cho.chonotes.framework.presentation.folderlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cho.chonotes.R
import cho.chonotes.business.domain.model.Folder
import cho.chonotes.framework.presentation.common.changeColor
import kotlinx.android.synthetic.main.layout_folder_list_item.view.*

class FolderListAdapter(
    private val interaction: Interaction? = null,
    private val lifecycleOwner: LifecycleOwner,
    private val selectedFolders: LiveData<ArrayList<Folder>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private val diffCallback = object : DiffUtil.ItemCallback<Folder>() {

        override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem.folder_id == newItem.folder_id
        }

        override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_folder_list_item,
                parent,
                false
            ),
            interaction,
            lifecycleOwner,
            selectedFolders
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FolderViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Folder>) {
        val commitCallback = Runnable {
            interaction?.restoreListPosition()
        }
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
        private val selectedFolders: LiveData<ArrayList<Folder>>
    ) : RecyclerView.ViewHolder(itemView) {

        private val colorGrey = R.color.blue_lighten
        private val colorPrimary = R.color.colorPrimary
        private lateinit var folder: Folder
        private lateinit var notesCount: String
        private lateinit var notesString: String

        fun bind(item: Folder) = with(itemView) {

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
            notesCount = item.notes_count.toString()
            notesString = when(item.notes_count > 1) {
                true -> "$notesCount notes"
                false -> "$notesCount note"
            }

            folder_timestamp.text = notesString

            selectedFolders.observe(lifecycleOwner, Observer { folders ->

                if(folders != null){
                    if(folders.contains(folder)){
                        changeColor(
                            newColor = colorGrey
                        )
                    }
                    else{
                        changeColor(
                            newColor = colorPrimary
                        )
                    }
                }else{
                    changeColor(
                        newColor = colorPrimary
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