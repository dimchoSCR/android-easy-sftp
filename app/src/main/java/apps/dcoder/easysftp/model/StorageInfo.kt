package apps.dcoder.easysftp.model

abstract class StorageInfo {
    abstract val id: String
    abstract val volumePath: String
    abstract val description: String
}