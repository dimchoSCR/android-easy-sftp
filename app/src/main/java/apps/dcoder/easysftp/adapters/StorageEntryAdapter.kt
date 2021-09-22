package apps.dcoder.easysftp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.model.androidModel.AdaptableLocalStorageInfo
import apps.dcoder.easysftp.model.androidModel.AdaptableRemoteStorageInfo
import apps.dcoder.easysftp.model.androidModel.AdaptableStorageInfo
import kotlinx.android.synthetic.main.rv_storage_item.view.*

class StorageEntryAdapter : RecyclerView.Adapter<StorageEntryAdapter.StorageViewHolder>() {

    private val storageItems: MutableList<AdaptableStorageInfo> = mutableListOf()
    var onItemClickListener: ((position: Int) -> Unit)? = null
    var onItemLongClickListener: ((position: Int, view: View) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_storage_item, parent, false)

        val viewHolder = StorageViewHolder(itemView)
        itemView.setOnClickListener {
            onItemClickListener?.invoke(viewHolder.absoluteAdapterPosition)
        }

        itemView.setOnLongClickListener {
            onItemLongClickListener?.invoke(viewHolder.absoluteAdapterPosition, it)
            true
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: StorageViewHolder, position: Int) {
        holder.bind(storageItems[position])
    }

    override fun getItemCount(): Int {
        return storageItems.size
    }

    override fun getItemId(position: Int): Long {
        return storageItems[position].hashCode().toLong()
    }

    fun getItem(index: Int): AdaptableStorageInfo {
        return storageItems[index]
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setStorageEntries(storageEntries: List<AdaptableStorageInfo>) {
        storageItems.clear()
        storageItems.addAll(storageEntries)

        notifyDataSetChanged()
    }

    fun insertStorageEntries(storageEntries: List<AdaptableStorageInfo>, startPos: Int, itemCount: Int) {
        storageItems.clear()
        storageItems.addAll(storageEntries)
        notifyItemRangeInserted(startPos, itemCount)
    }

    fun deleteStorageEntry(position: Int) {
        storageItems.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class StorageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivStorageIcon = itemView.imageView
        private val tvStorageText = itemView.info_text
        private val tvDetails = itemView.tvDetails

        fun bind(storageInfo: AdaptableStorageInfo) {
            ivStorageIcon.setImageResource(storageInfo.storageIconRes)
            tvDetails.text = storageInfo.volumePath
            when (storageInfo) {
                is AdaptableLocalStorageInfo -> {
                    tvStorageText.text = storageInfo.storageName
                }

                is AdaptableRemoteStorageInfo -> {
                    tvStorageText.text = storageInfo.name
                }
            }
        }
    }
}