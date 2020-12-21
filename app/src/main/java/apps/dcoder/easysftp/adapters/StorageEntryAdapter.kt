package apps.dcoder.easysftp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.model.androidModel.AdaptableLocalStorageInfo
import apps.dcoder.easysftp.model.androidModel.AdaptableRemoteStorageInfo
import apps.dcoder.easysftp.model.androidModel.AdaptableStorageInfo

class StorageEntryAdapter : BaseAdapter() {

    private val storageItems: MutableList<AdaptableStorageInfo> = mutableListOf()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val rootView: View = convertView
            ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_storage_item, parent, false)

        val ivStorageIcon = rootView.findViewById<ImageView>(R.id.imageView)
        val tvStorageText = rootView.findViewById<TextView>(R.id.info_text)
        val storageInfo: AdaptableStorageInfo = getItem(position)

        ivStorageIcon.setImageResource(storageInfo.storageIconRes)
        val translatableStorageLabel = storageInfo
        when (storageInfo) {
            is AdaptableLocalStorageInfo -> {
                tvStorageText.text = storageInfo.storageName
            }

            is AdaptableRemoteStorageInfo -> {
                tvStorageText.text = storageInfo.name
            }
        }

        return rootView

    }

    override fun getItem(position: Int): AdaptableStorageInfo {
        return storageItems[position]
    }

    override fun getItemId(position: Int): Long {
        return storageItems[position].hashCode().toLong()
    }

    override fun getCount(): Int {
        return storageItems.size
    }

    fun setStorageEntries(storageEntries: List<AdaptableStorageInfo>) {
        storageItems.clear()
        storageItems.addAll(storageEntries)

        notifyDataSetChanged()
    }
}