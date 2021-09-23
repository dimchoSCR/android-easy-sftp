package apps.dcoder.easysftp.viewmodels

import androidx.lifecycle.*
import apps.dcoder.easysftp.fragments.StorageAddDialogFragment
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

    val storageOptionsLiveData = liveData {
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