package apps.dcoder.easysftp.repos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.model.LocalStorageInfo
import apps.dcoder.easysftp.model.StorageInfo
import apps.dcoder.easysftp.services.storage.RemovableMediaState
import apps.dcoder.easysftp.services.storage.SharedPrefsStorageService
import apps.dcoder.easysftp.services.storage.StorageDiscoveryService
import apps.dcoder.easysftp.services.storage.listeners.OnRemovableMediaStateChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class StorageRepository: KoinComponent {

    private val storageDiscoveryService: StorageDiscoveryService by inject()
    private val sharedPrefsStorageService: SharedPrefsStorageService by inject()

    private var _storageOptionsLiveData = MutableLiveData<List<StorageInfo>>()

    suspend fun getStorageOptionsLiveDataSource(): LiveData<List<StorageInfo>> {
        getAllStorageOptions()
        return _storageOptionsLiveData
    }

    suspend fun refreshStorageOptions(pathToMedia: String, mediaState: RemovableMediaState) {
        // TODO use method parameters to remove and add storage options from cache
        getAllStorageOptions()
    }

    fun listenForRemovableStorageStateChanges(onRemovableMediaStateChanged: OnRemovableMediaStateChanged) {
        storageDiscoveryService.setOnRemovableMediaStateChangedListener(onRemovableMediaStateChanged)
    }

    private suspend fun getAllStorageOptions() = withContext(Dispatchers.Default) {
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

                updateStorageOptionsLiveData(availableStorageList)
            }

            updateStorageOptionsLiveData(availableStorageList)
        } catch (exc: NoSuchMethodException) {
            Log.e(this.javaClass.simpleName, "Error while gathering info for storage devices!", exc)
            updateStorageOptionsLiveData(emptyList())
        }
    }

    private suspend fun updateStorageOptionsLiveData(data: List<StorageInfo>) = withContext(Dispatchers.Main) {
        _storageOptionsLiveData.value = data
    }
}