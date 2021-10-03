package apps.dcoder.easysftp.repos

import android.util.Log
import apps.dcoder.easysftp.extensions.*
import apps.dcoder.easysftp.model.LocalStorageInfo
import apps.dcoder.easysftp.model.RemoteStorageInfo
import apps.dcoder.easysftp.model.StorageInfo
import apps.dcoder.easysftp.services.storage.RemovableMediaState
import apps.dcoder.easysftp.services.storage.SharedPrefsStorageService
import apps.dcoder.easysftp.services.storage.StorageDiscoveryService
import apps.dcoder.easysftp.services.storage.listeners.OnRemovableMediaStateChanged
import apps.dcoder.easysftp.util.LiveResource
import apps.dcoder.easysftp.util.MutableLiveResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class StorageRepository(
    private val storageDiscoveryService: StorageDiscoveryService,
    private val sharedPrefsStorageService: SharedPrefsStorageService
) {
    private var _storageOptionsLiveData = MutableLiveResource<List<StorageInfo>>()
    private val cachedStorageInfo = mutableListOf<StorageInfo>()

    fun getStorageOptionsLiveDataSource(): LiveResource<List<StorageInfo>> {
        return _storageOptionsLiveData
    }

    suspend fun refreshStorageOptions(pathToMedia: String, mediaState: RemovableMediaState) {
        withContext(Dispatchers.IO) {
            if (mediaState == RemovableMediaState.UNMOUNTED) {
                for (i in 0 until cachedStorageInfo.size) {
                    if (cachedStorageInfo[i].volumePath == pathToMedia) {
                        cachedStorageInfo.removeAt(i)
                        break
                    }
                }

                _storageOptionsLiveData.dispatchSuccessOnMain(cachedStorageInfo)
            } else {
                // TODO optimize get info for volume with the specified path
                getAllStorageOptions()
            }
        }
    }

    fun listenForRemovableStorageStateChanges(onRemovableMediaStateChanged: OnRemovableMediaStateChanged) {
        storageDiscoveryService.setOnRemovableMediaStateChangedListener(onRemovableMediaStateChanged)
    }

    suspend fun getAllStorageOptions() = withContext(Dispatchers.Default) {
        try {
            _storageOptionsLiveData.dispatchLoadingOnMain()

            val availableStorageList = mutableListOf<StorageInfo>()

            // Load local storage options first
            availableStorageList.addAll(getLocalStorageOptions())

            // Load remote storage options from shared prefs
            availableStorageList.addAll(getRemoteStorageOptions())
            cachedStorageInfo.clear()
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
        for (volume in mountedVolumes) {
            val isRemovable = storageDiscoveryService.isVolumeRemovable(volume)

            val volumePath = storageDiscoveryService.discoverVolumePath(volume)
            availableStorageList.add(LocalStorageInfo(
                volumePath,
                volumePath,
                storageDiscoveryService.discoverVolumeDescription(volume),
                isRemovable
            ))
        }

        return availableStorageList
    }

    private fun getRemoteStorageOptions(): List<StorageInfo> {
        val availableStorageList = mutableListOf<RemoteStorageInfo>()
        val savedStorageOptions = getSavedRemoteStorageOptions()
        for (savedOption in savedStorageOptions) {
            val data = savedOption.split(',')
            if (data.size < 4 || data.size > 4) {
                Log.e(this::class.java.simpleName, "Saved Storage option has too little or too many!")
                continue
            }

            val ip = data[0]
            val user = data[1]
            val name = data[2]
            val position = data[3].toInt()

            availableStorageList.add(
                RemoteStorageInfo(ip, "$user@$ip", "", name, user, ip, position)
            )
        }

        availableStorageList.sortBy { it.position }
        return availableStorageList
    }

    fun addStorageOption(serverIp: String, user: String, name: String) {
        if (sharedPrefsStorageService.containsKey(serverIp)) {
            _storageOptionsLiveData.setError("The remote storage your adding has already been added!")
            return
        }

        val position = cachedStorageInfo.size + 1
        storeRemoteStorageOption(serverIp, user, name, position)
        cachedStorageInfo.add(RemoteStorageInfo(serverIp, "$user@$serverIp" , "", name, user, serverIp, position))
        _storageOptionsLiveData.setSuccess(cachedStorageInfo)
    }

    private fun storeRemoteStorageOption(serverIp: String, user: String, name: String, position: Int) {
        sharedPrefsStorageService.putString(serverIp,"$serverIp,$user,$name,$position")
    }

    private fun getSavedRemoteStorageOptions(): List<String> {
        val storageOptions = sharedPrefsStorageService.readAll()
        val savedRemoteStorageList = mutableListOf<String>()
        for (key in storageOptions.keys) {
            if (key.contains('/')) {
                val splitKey = key.split(Pattern.compile("/"), 2)
                if (splitKey.size != 2) {
                    throw Exception("Bad path!")
                }

                if (splitKey[0].isIP()) {
                    storageOptions[key]?.let {
                        if (String::class.java.isAssignableFrom(it::class.java)) {
                            savedRemoteStorageList.add(it as String)
                        } else {
                            Log.e(
                                this::class.java.simpleName,
                                "Could not assign shared prefs value into Set<String>"
                            )
                        }
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
                _storageOptionsLiveData.dispatchSuccessOnMain(cachedStorageInfo)
                return@withContext
            }
        }
    }
}