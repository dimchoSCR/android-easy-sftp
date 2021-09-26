package apps.dcoder.easysftp.viewmodels

import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import apps.dcoder.easysftp.adapters.diff.StorageDiffCallback
import apps.dcoder.easysftp.fragments.StorageAddDialogFragment
import apps.dcoder.easysftp.model.androidModel.AdaptableStorageInfo
import apps.dcoder.easysftp.repos.StorageRepository
import apps.dcoder.easysftp.services.storage.RemovableMediaState
import apps.dcoder.easysftp.services.storage.listeners.OnRemovableMediaStateChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StorageListViewModel(private val storageRepo: StorageRepository) : ViewModel(),
    StorageAddDialogFragment.DialogActionListener {

    var selctedStorageIndex = -1

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

    private val _diffCompleteLiveData = MutableLiveData<Pair<List<AdaptableStorageInfo>,DiffUtil.DiffResult>>()
    val diffCompleteLiveData: LiveData<Pair<List<AdaptableStorageInfo>,DiffUtil.DiffResult>>
        get() = _diffCompleteLiveData

    fun calculateDiff(oldList: List<AdaptableStorageInfo>, newList: List<AdaptableStorageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            val diffResult = DiffUtil.calculateDiff(StorageDiffCallback(oldList, newList))
            _diffCompleteLiveData.postValue(Pair(newList, diffResult))
        }
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