package apps.dcoder.easysftp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.adapters.diff.StorageDiffCallback
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

    fun getAdaptedItems(): List<AdaptableStorageInfo> = storageItems

    fun updateStorageEntries(newStorageItems: List<AdaptableStorageInfo>, diffResult: DiffUtil.DiffResult) {
        storageItems.clear()
        storageItems.addAll(newStorageItems)
        diffResult.dispatchUpdatesTo(this)
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