package apps.dcoder.easysftp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import apps.dcoder.easysftp.filemanager.FileManager

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import java.io.File
import java.util.Stack

enum class ProgressState {
    LOADING, IDLE
}
class FileViewViewModel(val fileManager: FileManager, val rootDirPath: String)
    : ViewModel(), KoinComponent, FileManager by fileManager {

    val stateStack: Stack<Pair<Int, Int>> = Stack()
    var currentDirPath: String = rootDirPath

    private val _progressState = MutableLiveData(ProgressState.IDLE)
    var progressState: LiveData<ProgressState> = _progressState

    fun updateProgressState(progressState: ProgressState) {
        _progressState.value = progressState
    }
}