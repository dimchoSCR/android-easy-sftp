package apps.dcoder.easysftp.model.androidModel

import androidx.annotation.DrawableRes
import apps.dcoder.easysftp.model.RemoteStorageInfo

data class AdaptableRemoteStorageInfo(
    override val id: String,
    override val volumePath: String,
    override val description: String,
    @DrawableRes override val storageIconRes: Int,
    val name: String,
    val userName: String,
    val ip: String
) : AdaptableStorageInfo() {
    companion object {
        fun fromRemoteStorageInfo(remoteStorageInfo: RemoteStorageInfo, @DrawableRes storageIcon: Int): AdaptableRemoteStorageInfo {
            return AdaptableRemoteStorageInfo(
                remoteStorageInfo.id,
                remoteStorageInfo.volumePath,
                remoteStorageInfo.description,
                storageIcon,
                remoteStorageInfo.name,
                remoteStorageInfo.userName,
                remoteStorageInfo.ip
            )
        }
    }
}