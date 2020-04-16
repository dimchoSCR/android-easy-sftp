package apps.dcoder.easysftp.services.storage

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import apps.dcoder.easysftp.services.storage.listeners.OnRemovableMediaStateChanged
import java.lang.reflect.Method

class StorageDiscoveryService(private val appContext: Context) {

    private var onRemovableMediaStateChangedListener: OnRemovableMediaStateChanged? = null

    companion object {
        private const val STORAGE_MANAGER_GET_VOLUMES_METHOD_NAME = "getVolumeList"
        private const val VOLUME_GET_PATH_METHOD_NAME = "getPath"
        private const val VOLUME_GET_DESCRIPTION_METHOD_NAME = "getDescription"
        private const val VOLUME_GET_STATE_METHOD_NAME = "getState"
        private const val VOLUME_IS_REMOVABLE_METHOD_NAME = "isRemovable"
    }

    private fun getMethodFromObject(obj: Any, methodName: String, vararg parameters: Class<*>): Method {
        return obj.javaClass.getMethod(methodName, *parameters)
    }

    @Suppress("UNCHECKED_CAST")
    fun discoverAllStorageVolumes(): List<StorageVolume> {
        val storageManager = appContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return storageManager.storageVolumes
        }

        // Use reflection to reliably find out all mounted volume paths for older version of android
        val getVolumePathsMethod =
            getMethodFromObject(storageManager, STORAGE_MANAGER_GET_VOLUMES_METHOD_NAME)

        return (getVolumePathsMethod.invoke(storageManager) as Array<StorageVolume>).toList()
    }

    fun discoverMountedStorageVolumes(): List<StorageVolume> {
        return discoverAllStorageVolumes().filter {
            if (isVolumeRemovable(it)) {
                return@filter discoverVolumeState(it) == Environment.MEDIA_MOUNTED
            }

            return@filter true
        }
    }

    fun discoverVolumePath(storageVolume: StorageVolume): String {
        return getMethodFromObject(storageVolume, VOLUME_GET_PATH_METHOD_NAME)
            .invoke(storageVolume) as String
    }

    fun discoverVolumeDescription(storageVolume: StorageVolume): String {
        return getMethodFromObject(storageVolume, VOLUME_GET_DESCRIPTION_METHOD_NAME, Context::class.java)
            .invoke(storageVolume, appContext) as String
    }

    fun discoverVolumeState(storageVolume: StorageVolume): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
           return storageVolume.state
        }

        return getMethodFromObject(storageVolume, VOLUME_GET_STATE_METHOD_NAME)
            .invoke(storageVolume) as String
    }

    fun isVolumeRemovable(storageVolume: StorageVolume): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return storageVolume.isRemovable
        }

        return getMethodFromObject(storageVolume, VOLUME_IS_REMOVABLE_METHOD_NAME)
            .invoke(storageVolume) as Boolean
    }

    fun notifyRemovableMediaSateChange(mediaPath: String, mediaState: RemovableMediaState) {
        val pathWitRemovedScheme = mediaPath.replace(Regex("\\w*:/{0,2}"), "")
        onRemovableMediaStateChangedListener?.onMediaStateChanged(pathWitRemovedScheme, mediaState)
    }

    fun setOnRemovableMediaStateChangedListener(listener: OnRemovableMediaStateChanged) {
        onRemovableMediaStateChangedListener = listener
    }
}