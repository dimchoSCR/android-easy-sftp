package apps.dcoder.easysftp.adapters.diff

import androidx.recyclerview.widget.DiffUtil
import apps.dcoder.easysftp.model.androidModel.AdaptableLocalStorageInfo
import apps.dcoder.easysftp.model.androidModel.AdaptableRemoteStorageInfo
import apps.dcoder.easysftp.model.androidModel.AdaptableStorageInfo
import java.lang.IllegalStateException

class StorageDiffCallback(
    private val oldDataSet: List<AdaptableStorageInfo>,
    private val newDataSet: List<AdaptableStorageInfo>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldDataSet.size

    override fun getNewListSize(): Int = newDataSet.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean  {
        return oldDataSet[oldItemPosition].id == newDataSet[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldDataSet[oldItemPosition]
        val newItem = newDataSet[newItemPosition]

        if (oldItem is AdaptableLocalStorageInfo && newItem is AdaptableLocalStorageInfo) {
            return oldItem == newItem
        }

        if (oldItem is AdaptableRemoteStorageInfo && newItem is AdaptableRemoteStorageInfo) {
            return oldItem == newItem
        }

        throw IllegalStateException("Comparing items of two different types")
    }
}