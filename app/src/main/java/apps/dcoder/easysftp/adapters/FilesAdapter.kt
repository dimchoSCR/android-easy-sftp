package apps.dcoder.easysftp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.filemanager.ListItemClickListener
import apps.dcoder.easysftp.model.FileInfo
import kotlinx.android.synthetic.main.rv_file_item.view.*

class FilesAdapter(
    private val listener: ListItemClickListener
) : RecyclerView.Adapter<FilesAdapter.FilesViewHolder>() {

    private val files: MutableList<FileInfo> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_file_item, parent, false)

        return FilesViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) {
        val currentFileInfo = files[position]

        holder.ivFileIcon.setImageResource(currentFileInfo.imgResource)
        holder.tvFileType.text = currentFileInfo.fileType.value
        holder.tvFileName.text = currentFileInfo.name
        holder.tvLastEdit.text = currentFileInfo.lastEdit
    }

    fun getFileList() : List<FileInfo> {
        return files
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFileList(list: List<FileInfo>) {
        files.clear()
        files.addAll(list)
        notifyDataSetChanged()
    }

    inner class FilesViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val ivFileIcon: ImageView = itemView.ivFileIcon
        val tvFileName: TextView = itemView.tvFileName
        val tvFileType: TextView = itemView.tvFileType
        val tvLastEdit: TextView = itemView.tvLastEdit

        init {
            itemView.setOnClickListener(this)
        }

        // On recycler view item click
        override fun onClick(v: View?) {
            listener.onListItemClick(absoluteAdapterPosition)
        }
    }
}