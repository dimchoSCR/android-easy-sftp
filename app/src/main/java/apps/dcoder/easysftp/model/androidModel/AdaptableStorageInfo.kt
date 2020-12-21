package apps.dcoder.easysftp.model.androidModel

import apps.dcoder.easysftp.model.StorageInfo

abstract class AdaptableStorageInfo: StorageInfo() {
    abstract val storageIconRes: Int
}