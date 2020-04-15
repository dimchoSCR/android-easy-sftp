package apps.dcoder.easysftp.model

abstract class StorageInfo() {
    abstract val id: String
    abstract val volumePath: String
    abstract val description: String
    abstract val storageNameResID: Int
    abstract val storageIconResource: Int
}