package apps.dcoder.easysftp.services.android

import CoroutineService
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import apps.dcoder.easysftp.extensions.launchCancellable
import apps.dcoder.easysftp.filemanager.FileManager
import apps.dcoder.easysftp.filemanager.remote.RemoteFileManager
import apps.dcoder.easysftp.model.FileInfo
import apps.dcoder.easysftp.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

enum class FileManagerType {
    LOCAL, REMOTE
}

sealed class FileManagerOperationResult private constructor() {
    data class ListOperationResult(val files: List<FileInfo>): FileManagerOperationResult()
}

class FileManagerService : CoroutineService(), KoinComponent {

    private val binder = FileManagerBinder()

    private var localRootDirPath = ""
    private var remoteRootDirPath = ""

    private val localFileManager: FileManager by inject {
        parametersOf(FileManagerType.LOCAL, localRootDirPath)
    }
    private val remoteFileManager: FileManager by inject {
        parametersOf(FileManagerType.REMOTE, remoteRootDirPath)
    }

    private val _fileManagerOperationLiveData = MutableLiveResource<FileManagerOperationResult>()
    val fileManagerOperationLiveData: LiveResource<FileManagerOperationResult> = _fileManagerOperationLiveData

    private val _sshPassRequestEvent = MutableLiveEvent<Unit>()
    val sshPassRequestEvent: LiveEvent<Unit> = _sshPassRequestEvent

    private lateinit var currentFileManager: FileManager

    private var currentRunningJob: Job? = null

    override fun onStartService(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
       return binder
    }

    fun prepare(onPrepared: () -> Unit) {
        currentFileManager.prepare(onPrepared)
    }

    private fun cancelCurrentAndDoAsync(action: suspend () -> Unit) {
        currentRunningJob?.let {
            launch(Dispatchers.IO) {
                currentFileManager.onCurrentOperationCancelled()
            }

            it.cancel()
            currentRunningJob = null
        }

        currentRunningJob = launchCancellable {
            action()
            currentRunningJob = null
        }
    }

    fun listCurrentDirectory(forceRefresh: Boolean = false) = cancelCurrentAndDoAsync {
        sendOperationResult(currentFileManager.listCurrentDir(forceRefresh))
    }

    fun listDirectory(dirPath: String) = cancelCurrentAndDoAsync {
        sendOperationResult(currentFileManager.listDirectory(dirPath))
    }

    fun listParent() = cancelCurrentAndDoAsync {
        sendOperationResult(currentFileManager.listParent())
    }

    fun isOnRootDir(): Boolean {
        return currentFileManager.isOnRootDir()
    }

    private suspend fun sendOperationResult(files: List<FileInfo>) {
        _fileManagerOperationLiveData.dispatchSuccessOnMain(FileManagerOperationResult.ListOperationResult(files))
    }

    fun getParentDirectoryPath(dir: String): String {
        return currentFileManager.getParentDirectoryPath(dir)
    }

    fun getCurrentlyListedFiles(): List<FileInfo> {
        return currentFileManager.getCurrentlyListedFiles()
    }

    fun getCachedFolder(dirPath: String): List<FileInfo>? {
        return currentFileManager.getCachedFolder(dirPath)
    }

    fun getCacheSize(): Int {
        return currentFileManager.filesCache.size
    }

    fun setCurrentDir(dirPath: String) {
        currentFileManager.currentDir = dirPath
    }

    fun restoreFileManagerStateAfterServiceRestart(
        lastListedDir: String,
        listedFiles: List<FileInfo>
    ) = launch(Dispatchers.IO) {
        setCurrentDir(lastListedDir)
        // Copy list entries to avoid reference problems
        val newList = mutableListOf<FileInfo>()
        newList.addAll(listedFiles)
        currentFileManager.putInCache(lastListedDir, newList)
    }

    fun getCurrentDir(): String {
        return currentFileManager.currentDir
    }

    fun setRemotePassword(pass: String) {
        (remoteFileManager as RemoteFileManager).setSshPassword(pass)
    }

    inner class FileManagerBinder : Binder() {
        fun getService(rootDirectory: String): FileManagerService {
            if (rootDirectory.contains('@')) {
                remoteRootDirPath = rootDirectory
                currentFileManager = remoteFileManager
                val castRemoteFileManager = (remoteFileManager as RemoteFileManager)
                castRemoteFileManager.onPasswordRequested = {
                    _sshPassRequestEvent.postValue(Event(Unit))
                }
            } else {
                localRootDirPath = rootDirectory
                currentFileManager = localFileManager
            }

            return this@FileManagerService
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DMK", "Exiting file managers")
        if (localRootDirPath != "") {
            localFileManager.exit()
        }

        if (remoteRootDirPath != "") {
            remoteFileManager.exit()
        }
    }
}