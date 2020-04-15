package apps.dcoder.easysftp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.model.StorageInfo

class StorageEntryAdapter : BaseAdapter() {

    private val storageItems: MutableList<StorageInfo> = mutableListOf()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val rootView: View = convertView
            ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_storage_ietm, parent, false)

        val ivStorageIcon = rootView.findViewById<ImageView>(R.id.imageView)
        val tvStorageText = rootView.findViewById<TextView>(R.id.info_text)
        val storageInfo: StorageInfo = getItem(position)

        ivStorageIcon.setImageResource(storageInfo.storageIconResource)
        val translatableStorageLabel = rootView.resources.getString(storageInfo.storageNameResID)
        tvStorageText.text = rootView.resources.getString(R.string.storage_name_template,
            translatableStorageLabel,
            storageInfo.description
        )

        return rootView

    }

    override fun getItem(position: Int): StorageInfo {
        return storageItems[position]
    }

    override fun getItemId(position: Int): Long {
        return storageItems[position].hashCode().toLong()
    }

    override fun getCount(): Int {
        return storageItems.size
    }

    fun setStorageEntries(storageEntries: List<StorageInfo>) {
        storageItems.clear()
        storageItems.addAll(storageEntries)

        notifyDataSetChanged()
    }
}