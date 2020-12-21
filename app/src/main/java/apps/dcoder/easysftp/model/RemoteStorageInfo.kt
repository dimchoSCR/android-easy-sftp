package apps.dcoder.easysftp.model

data class RemoteStorageInfo(
    override val id: String,
    override val volumePath: String,
    override val description: String,
    val name: String,
    val userName: String,
    val ip: String
) : StorageInfo()