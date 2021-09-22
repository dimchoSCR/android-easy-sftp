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

class StorageRepository(
    private val storageDiscoveryService: StorageDiscoveryService,
    private val sharedPrefsStorageService: SharedPrefsStorageService
) {
    private var _storageOptionsLiveData = MutableLiveData<Resource<List<StorageInfo>, Int>>()
    private val cachedStorageInfo = mutableListOf<StorageInfo>()

    fun getStorageOptionsLiveDataSource(): LiveData<Resource<List<StorageInfo>, Int>> {
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

            val availableStorageList = mutableListOf<StorageInfo>()

            // Display local storage options first
            availableStorageList.addAll(getLocalStorageOptions())
            _storageOptionsLiveData.dispatchSuccessOnMain(availableStorageList)

            // Display remote storage options from shared prefs
            availableStorageList.addAll(getRemoteStorageOptions())
            cachedStorageInfo.addAll(availableStorageList)
            _storageOptionsLiveData.dispatchSuccessOnMain(availableStorageList)

        } catch (exc: Exception) {
            Log.e(this.javaClass.simpleName, "Error while gathering info for storage devices!", exc)
            _storageOptionsLiveData.dispatchErrorOnMain(emptyList(), "")
        }
    }

    private fun getLocalStorageOptions(): List<StorageInfo> {
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

        return availableStorageList
    }

    private fun getRemoteStorageOptions(): List<StorageInfo> {
        val availableStorageList = mutableListOf<StorageInfo>()
        val savedStorageOptions = getSavedRemoteStorageOptions()
        for (savedOption in savedStorageOptions) {
            val data = savedOption.split(',')
            if (data.size < 3) {
                Log.e(this::class.java.simpleName, "Saved Storage option has too few parameters!")
                continue
            }

            val ip = data[0]
            val user = data[1]
            val name = data[2]

            availableStorageList.add(
                RemoteStorageInfo(ip, "$user@$ip", "", name, user, ip)
            )
        }

        return availableStorageList
    }

    fun addStorageOption(serverIp: String, user: String, name: String) {
        if (sharedPrefsStorageService.containsKey(serverIp)) {
            _storageOptionsLiveData.setError("The remote stoeage tyour adding has already been added!")
            return
        }

        storeRemoteStorageOption(serverIp, user, name)
        cachedStorageInfo.add(RemoteStorageInfo(serverIp, "$user@$serverIp" , "", name, user, serverIp))
        _storageOptionsLiveData.setSuccess(cachedStorageInfo)
    }

    private fun storeRemoteStorageOption(serverIp: String, user: String, name: String) {
        sharedPrefsStorageService.putString(serverIp,"$serverIp,$user,$name")
    }

    private fun getSavedRemoteStorageOptions(): List<String> {
        val storageOptions = sharedPrefsStorageService.readAll()
        val savedRemoteStorageList = mutableListOf<String>()
        for (key in storageOptions.keys) {
            if (key.isIP()) {
                storageOptions[key]?.let {
                    if (String::class.java.isAssignableFrom(it::class.java)) {
                        savedRemoteStorageList.add(it as String)
                    } else {
                        Log.e(this::class.java.simpleName, "Could not assign shared prefs value into Set<String>")
                    }
                }
            }
        }

        return savedRemoteStorageList
    }

    suspend fun removeRemoteStorage(ip : String) = withContext(Dispatchers.IO) {
        for (i in 0 until cachedStorageInfo.size) {
            if (cachedStorageInfo[i].id == ip){
                sharedPrefsStorageService.removeKey(ip, false)
                cachedStorageInfo.removeAt(i)
                _storageOptionsLiveData.dispatchSuccessOnMain(cachedStorageInfo, i)
                return@withContext
            }
        }
    }

    companion object {
        private const val KEY_OPTION_IP = "KEY_OPTION_IP";
        private const val KEY_OPTION_USER = "KEY_OPTION_USER";
        private const val KEY_OPTION_DISPLAY_NAME = "KEY_OPTION_DISPLAY_NAME";
    }
}