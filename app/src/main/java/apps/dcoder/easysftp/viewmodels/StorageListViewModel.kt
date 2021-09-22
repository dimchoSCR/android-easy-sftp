package apps.dcoder.easysftp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import apps.dcoder.easysftp.fragments.StorageAddDialogFragment
import apps.dcoder.easysftp.model.StorageInfo
import apps.dcoder.easysftp.model.status.Resource
import apps.dcoder.easysftp.repos.StorageRepository
import apps.dcoder.easysftp.services.storage.RemovableMediaState
import apps.dcoder.easysftp.services.storage.listeners.OnRemovableMediaStateChanged
import kotlinx.coroutines.launch

class StorageListViewModel(private val storageRepo: StorageRepository) : ViewModel(),
    StorageAddDialogFragment.DialogActionListener {

    init {
        storageRepo.listenForRemovableStorageStateChanges(object : OnRemovableMediaStateChanged {
            override fun onMediaStateChanged(pathToMedia: String, mediaState: RemovableMediaState) {
                viewModelScope.launch {
                    storageRepo.refreshStorageOptions(pathToMedia, mediaState)
                }
            }
        })
    }

    val storageOptionsLiveData: LiveData<Resource<List<StorageInfo>, Int>> = liveData {
        emitSource(storageRepo.getStorageOptionsLiveDataSource())
        storageRepo.getAllStorageOptions()
    }

    fun removeRemoteStorageItem(ip: String) = viewModelScope.launch {
        storageRepo.removeRemoteStorage(ip)
    }

    override fun onDialogPositiveClick(result: Array<String>) {
        val serverIp = result[0]
        val user = result[1]
        val name = result[2]

        storageRepo.addStorageOption(serverIp, user, name)
    }
}