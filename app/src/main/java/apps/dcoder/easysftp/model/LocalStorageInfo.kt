package apps.dcoder.easysftp.model

open class LocalStorageInfo(
    override val id: String,
    override val volumePath: String,
    override val description: String,
    val isRemovable: Boolean
): StorageInfo()