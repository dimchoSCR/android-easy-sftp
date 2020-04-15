package apps.dcoder.easysftp.repos

import android.util.Log
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.model.LocalStorageInfo
import apps.dcoder.easysftp.model.StorageInfo
import apps.dcoder.easysftp.services.storage.SharedPrefsStorageService
import apps.dcoder.easysftp.services.storage.StorageDiscoveryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class StorageRepository: KoinComponent {

    private val storageDiscoveryService: StorageDiscoveryService by inject()
    private val sharedPrefsStorageService: SharedPrefsStorageService by inject()

    suspend fun getAllStorageOptions(): List<StorageInfo> = withContext(Dispatchers.Default) {
        try {
            // TODO get sftp server list from preferences
            val mountedVolumes = storageDiscoveryService.discoverMountedStorageVolumes()

            val availableStorageList = mutableListOf<StorageInfo>()
            for ((volumeIndex, volume) in mountedVolumes.withIndex()) {
                val isRemovable = storageDiscoveryService.isVolumeRemovable(volume)
                val (nameResource, imageResource) = if (isRemovable) {
                    Pair(R.string.removable_storage, R.drawable.ic_sd_storage_black_24dp)
                } else {
                    Pair(R.string.internal_storage, R.drawable.ic_storage_black_24dp)
                }

                availableStorageList.add(LocalStorageInfo(
                    volumeIndex.toString(),
                    storageDiscoveryService.discoverVolumePath(volume),
                    storageDiscoveryService.discoverVolumeDescription(volume),
                    nameResource,
                    imageResource,
                    isRemovable
                ))

            }

            return@withContext availableStorageList
        } catch (exc: NoSuchMethodException) {
            Log.e(this.javaClass.simpleName, "Error while gathering info for storage!", exc)
            return@withContext emptyList<StorageInfo>()
        }
    }
}