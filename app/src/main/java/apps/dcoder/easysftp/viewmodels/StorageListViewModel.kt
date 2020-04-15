package apps.dcoder.easysftp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import apps.dcoder.easysftp.model.StorageInfo
import apps.dcoder.easysftp.repos.StorageRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class StorageListViewModel: ViewModel(), KoinComponent {
    private val storageRepo: StorageRepository by inject()

    val storageOptionsLiveData: LiveData<List<StorageInfo>> = liveData {
        emit(storageRepo.getAllStorageOptions())
    }
}