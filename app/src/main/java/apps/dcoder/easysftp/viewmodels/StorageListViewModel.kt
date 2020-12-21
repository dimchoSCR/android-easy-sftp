package apps.dcoder.easysftp.viewmodels

import android.os.Bundle
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.IllegalStateException

class StorageListViewModel : ViewModel(), KoinComponent, StorageAddDialogFragment.DialogActionListener {
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

    override fun onDialogPositiveClick(result: Bundle) {
        val storageDetailsArr = result.getStringArray(StorageAddDialogFragment.KEY_STORAGE_DATA)
            ?: throw IllegalStateException("Missing data for key KEY_STORAGE_DATA")

        val serverIp = storageDetailsArr[0]
        val user = storageDetailsArr[1]
        val name = storageDetailsArr[2]

        storageRepo.addStorageOption(serverIp, user, name)
    }
}