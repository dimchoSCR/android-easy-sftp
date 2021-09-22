package apps.dcoder.easysftp.model.androidModel

import androidx.annotation.DrawableRes
import apps.dcoder.easysftp.model.LocalStorageInfo

data class AdaptableLocalStorageInfo(
    override val id: String,
    override val volumePath: String,
    override val description: String,
    val storageName: String,
    @DrawableRes override val storageIconRes: Int,
    val isRemovable: Boolean
) : AdaptableStorageInfo() {

    companion object {
        fun fromLocalStorageInfo(localStorageInfo: LocalStorageInfo, storageName: String, @DrawableRes storageIcon: Int): AdaptableLocalStorageInfo {
            return AdaptableLocalStorageInfo(
                localStorageInfo.id,
                localStorageInfo.volumePath,
                localStorageInfo.description,
                storageName,
                storageIcon,
                localStorageInfo.isRemovable
            )
        }
    }
}