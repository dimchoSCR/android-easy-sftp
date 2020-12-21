package apps.dcoder.easysftp.repos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import apps.dcoder.easysftp.extensions.*
import apps.dcoder.easysftp.model.LocalStorageInfo
import apps.dcoder.easysftp.model.RemoteStorageInfo
import apps.dcoder.easysftp.model.StorageInfo
import apps.dcoder.easysftp.model.status.Resource
import apps.dcoder.easysftp.services.storage.RemovableMediaState
import apps.dcoder.easysftp.services.storage.SharedPrefsStorageService
import apps.dcoder.easysftp.services.storage.StorageDiscoveryService
import apps.dcoder.easysftp.services.storage.listeners.OnRemovableMediaStateChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class StorageRepository : KoinComponent {

    private val storageDiscoveryService: StorageDiscoveryService by inject()
    private val sharedPrefsStorageService: SharedPrefsStorageService by inject()

    private var _storageOptionsLiveData = MutableLiveData<Resource<List<StorageInfo>>>()

    fun getStorageOptionsLiveDataSource(): LiveData<Resource<List<StorageInfo>>> {
        return _storageOptionsLiveData
    }

    suspend fun refreshStorageOptions(pathToMedia: String, mediaState: RemovableMediaState) {
        // TODO use method parameters to remove and add external storage options from cache
        getAllStorageOptions()
    }

    fun listenForRemovableStorageStateChanges(onRemovableMediaStateChanged: OnRemovableMediaStateChanged) {
        storageDiscoveryService.setOnRemovableMediaStateChangedListener(onRemovableMediaStateChanged)
    }

    suspend fun getAllStorageOptions() = withContext(Dispatchers.Default) {
        try {
            _storageOptionsLiveData.dispatchLoadingOnMain()

            // TODO get sftp server list from preferences
            val mountedVolumes = storageDiscoveryService.discoverMountedStorageVolumes()

            val availableStorageList = mutableListOf<StorageInfo>()
            for ((volumeIndex, volume) in mountedVolumes.withIndex()) {
                val isRemovable = storageDiscoveryService.isVolumeRemovable(volume)

                availableStorageList.add(LocalStorageInfo(
                    volumeIndex.toString(),
                    storageDiscoveryService.discoverVolumePath(volume),
                    storageDiscoveryService.discoverVolumeDescription(volume),
                    isRemovable
                ))
            }

            _storageOptionsLiveData.dispatchSuccessOnMain(availableStorageList)
        } catch (exc: Exception) {
            Log.e(this.javaClass.simpleName, "Error while gathering info for storage devices!", exc)
            _storageOptionsLiveData.dispatchErrorOnMain(emptyList(), "")
        }
    }

    fun addStorageOption(serverIp: String, user: String, name: String) {
        val list = _storageOptionsLiveData.value!!.data!!.toMutableList()

        list.add(RemoteStorageInfo("12", "" , "", name, user, serverIp))
        _storageOptionsLiveData.setSuccess(list)
    }
}