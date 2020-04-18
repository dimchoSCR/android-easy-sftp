package apps.dcoder.easysftp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import apps.dcoder.easysftp.model.StorageInfo
import apps.dcoder.easysftp.model.status.Resource
import apps.dcoder.easysftp.repos.StorageRepository
import apps.dcoder.easysftp.services.storage.RemovableMediaState
import apps.dcoder.easysftp.services.storage.listeners.OnRemovableMediaStateChanged
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class StorageListViewModel: ViewModel(), KoinComponent {
    private val storageRepo: StorageRepository by inject()

    init {
        storageRepo.listenForRemovableStorageStateChanges(object : OnRemovableMediaStateChanged {
            override fun onMediaStateChanged(pathToMedia: String, mediaState: RemovableMediaState) {
                viewModelScope.launch {
                    storageRepo.refreshStorageOptions(pathToMedia, mediaState)
                }
            }
        })
    }

    val storageOptionsLiveData: LiveData<Resource<List<StorageInfo>>> = liveData {
        emitSource(storageRepo.getStorageOptionsLiveDataSource())
        storageRepo.getAllStorageOptions()
    }
}