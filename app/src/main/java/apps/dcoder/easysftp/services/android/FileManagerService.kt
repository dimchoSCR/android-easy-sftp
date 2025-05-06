package apps.dcoder.easysftp.services.android

import CoroutineService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import apps.dcoder.easysftp.extensions.launchCancellable
import apps.dcoder.easysftp.filemanager.ClipBoardManager
import apps.dcoder.easysftp.filemanager.FileManager
import apps.dcoder.easysftp.filemanager.local.LocalFileManager
import apps.dcoder.easysftp.filemanager.remote.FileOperationStatusListener
import apps.dcoder.easysftp.filemanager.remote.RemoteFileManager
import apps.dcoder.easysftp.model.FileInfo
import apps.dcoder.easysftp.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

import android.app.Notification
import android.app.NotificationChannel
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import apps.dcoder.easysftp.services.storage.StorageDiscoveryService

enum class FileManagerType {
    LOCAL, REMOTE
}

sealed class FileManagerOperationResult private constructor() {
    data class ListOperationResult(val files: List<FileInfo>): FileManagerOperationResult()
    data class RenameOperationResult(val renamedFileInfo: FileInfo, val destIndex: Int): FileManagerOperationResult()
    open class RemoteDownloadOperationResult(val destinationPath: String?) : FileManagerOperationResult()
    object RemoteDownloadNoSuchFileResult : RemoteDownloadOperationResult(null)
    object DeleteOperationResult : FileManagerOperationResult()
}

class FileManagerService : CoroutineService(), KoinComponent {

    private val binder = FileManagerBinder()

    private var localRootDirPath = ""
    private var remoteRootDirPath = ""

    private var fileOpListener: FileOperationStatusListener? = null

    private val localFileManager: FileManager by inject {
        parametersOf(FileManagerType.LOCAL, localRootDirPath)
    }

    private val remoteFileManager: FileManager by inject {
        parametersOf(FileManagerType.REMOTE, remoteRootDirPath)
    }

    private val storageDiscoveryService: StorageDiscoveryService by inject()

    private val _fileManagerOperationLiveData = MutableLiveResource<FileManagerOperationResult>()
    val fileManagerOperationLiveData: LiveResource<FileManagerOperationResult> = _fileManagerOperationLiveData

    private val _sshPassRequestEvent = MutableLiveEvent<Unit>()
    val sshPassRequestEvent: LiveEvent<Unit> = _sshPassRequestEvent

    private val notificationManager: NotificationManager by lazy {
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

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

    fun isLocal(): Boolean {
        return currentFileManager is LocalFileManager
    }

    fun doPaste(clipBoardEntry: ClipBoardManager.ClipBoardEntry) = launch(Dispatchers.IO) {
        if (currentFileManager is LocalFileManager && !clipBoardEntry.isLocalFile) {
            downloadFileFromRemote(clipBoardEntry.filePath, clipBoardEntry.fileNameWithExt, currentFileManager.currentDir)
        } else {
            currentFileManager.paste(clipBoardEntry)
        }
    }

    fun downloadFileFromRemote(sourceFile: String, destFileName: String, destDir: String) = launch(Dispatchers.IO) {
        if (!remoteFileManager.exists(sourceFile)) {
            _fileManagerOperationLiveData.dispatchSuccessOnMain(FileManagerOperationResult.RemoteDownloadNoSuchFileResult)
            return@launch
        }

        val inputStreamAndSize = remoteFileManager.getInputStreamWithSize(sourceFile)
        localFileManager.fileOpListener = fileOpListener

        (localFileManager as LocalFileManager).pasteFromRemote(inputStreamAndSize, destFileName, destDir)
        _fileManagerOperationLiveData.dispatchSuccessOnMain(FileManagerOperationResult.RemoteDownloadOperationResult(null))
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
                val localFm = localFileManager as LocalFileManager
                if (localRootDirPath != rootDirectory) {
                    localRootDirPath = rootDirectory
                    localFm.changeLocalRootDir(rootDirectory)
                    localFm.currentDir = rootDirectory
                }

                currentFileManager = localFm
            }

            initFileManagerListeners()

            return this@FileManagerService
        }
    }

    private fun initFileManagerListeners() {
        val builder = getProgressNotificationBuilder("File Manager Operation", "Copying")

        fileOpListener = object : FileOperationStatusListener {
            override fun onOpStarted() {
                builder.setProgress(100, 0, false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, builder.build(), FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } else {
                    startForeground(NOTIFICATION_ID, builder.build())
                }
            }

            override fun onUpdateOpProgress(bytesTransferred: Long, totalBytes: Long) {
                val progress = (bytesTransferred * 100 / totalBytes).toInt()
                Log.d("Copy", "Progress $progress")
                builder.setProgress(100, progress, false)
                notificationManager.notify(NOTIFICATION_ID, builder.build())
            }

            override fun onOpComplete() {
                builder.setContentTitle("Operation Completed")
                    .setContentText("")
                    .setProgress(0, 0, false)

                notificationManager.notify(NOTIFICATION_ID, builder.build())
                ClipBoardManager.clear()
                // TODO remove this to fragment
                listCurrentDirectory(true)
                stopForeground(true)
            }
        }

        currentFileManager.fileOpListener = fileOpListener
    }

    private fun getProgressNotificationBuilder(title: String, opText: String): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sftp Operations",
                NotificationManager.IMPORTANCE_NONE
            )
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(opText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
    }

    fun renameCurrent(oldName: String, newName: String) = launch(Dispatchers.IO) {
        // TODO Handle op errors
        val renamedFileInfo = currentFileManager.rename(oldName, newName)
        val cachedFiles = currentFileManager.getCachedFolder(currentFileManager.currentDir)
        if (cachedFiles == null) {
            Log.e("DMK", "Error while renaming file")
            return@launch
        }

        val sortedIndexOfRenamedFile = cachedFiles.indexOf(renamedFileInfo)
        val result = FileManagerOperationResult.RenameOperationResult(renamedFileInfo, sortedIndexOfRenamedFile)
        _fileManagerOperationLiveData.dispatchSuccessOnMain(result)
    }

    fun delete(filePath: String) = launch(Dispatchers.IO) {
        currentFileManager.delete(filePath)
        _fileManagerOperationLiveData.dispatchSuccessOnMain(FileManagerOperationResult.DeleteOperationResult)
    }

    fun getLocalDownloadsFolder(): String {
        return "${storageDiscoveryService.discoverInternalStoragePath()}/Download"
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

    companion object {
        private const val CHANNEL_ID = "FileManager"
        private const val NOTIFICATION_ID = 1001
    }
}