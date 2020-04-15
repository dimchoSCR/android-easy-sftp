package apps.dcoder.easysftp.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class RemoteStorageInfo(
    override val id: String,
    override val volumePath: String,
    override val description: String,
    @StringRes override val storageNameResID: Int,
    @DrawableRes override val storageIconResource: Int,
    val userName: String,
    val ip: String
): StorageInfo()