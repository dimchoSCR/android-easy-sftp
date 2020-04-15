package apps.dcoder.easysftp.services.storage

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import java.lang.reflect.Method

class StorageDiscoveryService(private val appContext: Context) {

    companion object {
        private const val STORAGE_MANAGER_GET_VOLUMES_METHOD_NAME = "getVolumeList"
        private const val VOLUME_GET_PATH_METHOD_NAME = "getPath"
        private const val VOLUME_GET_DESCRIPTION_METHOD_NAME = "getDescription"
        private const val VOLUME_IS_REMOVABLE_METHOD_NAME = "isRemovable"
    }

    private fun getMethodFromObject(obj: Any, methodName: String, vararg parameters: Class<*>): Method {
        return obj.javaClass.getMethod(methodName, *parameters)
    }

    @Suppress("UNCHECKED_CAST")
    fun discoverMountedStorageVolumes(): List<StorageVolume> {
        val storageManager = appContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return storageManager.storageVolumes
        }

        // Use reflection to reliably find out all mounted volume paths for older version of android
        val getVolumePathsMethod =
            getMethodFromObject(storageManager, STORAGE_MANAGER_GET_VOLUMES_METHOD_NAME)

        return (getVolumePathsMethod.invoke(storageManager) as Array<StorageVolume>).toList()
    }

    fun discoverVolumePath(storageVolume: StorageVolume): String {
        return getMethodFromObject(storageVolume, VOLUME_GET_PATH_METHOD_NAME)
            .invoke(storageVolume) as String
    }

    fun discoverVolumeDescription(storageVolume: StorageVolume): String {
        return getMethodFromObject(storageVolume, VOLUME_GET_DESCRIPTION_METHOD_NAME, Context::class.java)
            .invoke(storageVolume, appContext) as String
    }

    fun isVolumeRemovable(storageVolume: StorageVolume): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return storageVolume.isRemovable
        }

        return getMethodFromObject(storageVolume, VOLUME_IS_REMOVABLE_METHOD_NAME)
            .invoke(storageVolume) as Boolean
    }

}