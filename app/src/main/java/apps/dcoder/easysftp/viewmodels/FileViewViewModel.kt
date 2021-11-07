package apps.dcoder.easysftp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apps.dcoder.easysftp.util.*
import com.github.junrar.Junrar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.Stack
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import apps.dcoder.easysftp.MainApplication
import apps.dcoder.easysftp.extensions.isAppInstalled
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

enum class ProgressState {
    LOADING, IDLE
}

enum class ArchiveType {
    ZIP, RAR
}

class FileViewViewModel(private val rootDirPath: String) : ViewModel() {

    private var prevItemPos = 0
    private var prevScrollOffset = 0
    private var indexOfRenamedFile = -1
    private var downloadsDir: String? = null

    private val lock = ReentrantLock()
    private val subsCheckAndDownload = lock.newCondition()

    private val _progressState = MutableLiveEvent(Event(ProgressState.LOADING))
    val progressState: LiveEvent<ProgressState> = _progressState

    private val _eventPasswordSet = MutableLiveEvent<String>()
    val eventPasswordSet: LiveEvent<String> = _eventPasswordSet

    private val _archiveExtractionEvent = MutableLiveResource<Unit>()
    val archiveExtractionEvent: LiveResource<Unit> = _archiveExtractionEvent

    private val _eventDownloadSubs = MutableLiveEvent<String>()
    val eventDownloadSubs: LiveEvent<String> = _eventDownloadSubs

    private val _eventPlayVideo = MutableLiveEvent<Intent>()
    val eventPlayVideo: LiveEvent<Intent> = _eventPlayVideo

    private val _eventInstallVLC = MutableLiveEvent<Unit>()
    val eventInstallVLC = _eventInstallVLC

    val positionStack: Stack<Pair<Int, Int>> = Stack()
    var shouldUnbindFileService = false
    var lastListedDir: String = rootDirPath
    private var lastClickedItemIndex = -1
    var serviceHasBeenKilled: Boolean = false

    fun setIndexOfFileRenamed(index: Int) {
        indexOfRenamedFile = index
    }

    fun getAndResetRenameIndex(): Int {
        val indexOfRenamedFile = this.indexOfRenamedFile
        this.indexOfRenamedFile = -1

        return indexOfRenamedFile
    }

    fun setLastClickedItemIndex(lastClickedIndex : Int) {
        lastClickedItemIndex = lastClickedIndex
    }

    fun getLastClickedItemIndex(): Int {
        val lastClicked = lastClickedItemIndex
        lastClickedItemIndex = -1

        return lastClicked
    }

    fun updateProgressState(progressState: ProgressState) {
        _progressState.value = Event(progressState)
    }

    fun popSavedScrollPositions() {
        if (positionStack.isNotEmpty()) {
            val positionPair = positionStack.pop()
            val (prevItemPos, prevScrollOffset) = positionPair

            this.prevItemPos = prevItemPos
            this.prevScrollOffset = prevScrollOffset
        }
    }

    fun getSavedScrollPositions(): Pair<Int, Int> {
        return Pair(prevItemPos, prevScrollOffset)
    }

    fun resetRvPositions() {
        prevItemPos = 0
        prevScrollOffset = 0
    }

    fun handleFileType(filePath: String, fileName: String, isLocal: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val indexOfLastDot = fileName.lastIndexOf('.')
        if (indexOfLastDot == -1) {
            Log.e("DMK", "File has nor file extension")
            return@launch
        }

        val ext = fileName.substring(indexOfLastDot + 1)
        val filePathWithoutExt = filePath.removeSuffix(".$ext")

        // TODO extraction progress
        when (ext.lowercase()) {
            "zip" -> {
                unArchiveIfPossible(ArchiveType.ZIP, filePath, filePathWithoutExt, isLocal)
            }

            "rar" -> {
                unArchiveIfPossible(ArchiveType.RAR, filePath, filePathWithoutExt, isLocal)
            }

            in videoFormats -> {
                playVideo(filePath, filePathWithoutExt, isLocal)
            }

            else -> {
                Log.d("DMK", "Not a supported format")
            }
        }
    }

    private suspend fun unArchiveIfPossible(
        archiveType: ArchiveType,
        filePath: String,
        filePathWithoutExt: String,
        isLocal: Boolean
    ) {
        if (!isLocal) {
            Log.d(this::class.java.simpleName, "Can not unzip on remote!")
            return
        }

        _archiveExtractionEvent.dispatchLoadingOnMain()
        when (archiveType) {
            ArchiveType.ZIP -> {
                unzip(filePath, filePathWithoutExt)
            }

            ArchiveType.RAR -> {
                unrar(filePath, filePathWithoutExt)
            }
        }

        _archiveExtractionEvent.dispatchSuccessOnMain(Unit)
    }

    private fun unzip(archivePath: String, destPath: String) {
        createUnArchiveDir(destPath)

        val zipIn = ZipInputStream(FileInputStream(archivePath))
        var currentEntry: ZipEntry? = zipIn.nextEntry
        // Iterates over entries in the zip file
        while (currentEntry != null) {
            val filePath: String = destPath + File.separator + currentEntry.name
            if (!currentEntry.isDirectory) {
                // If the entry is a file, extract it
                extractFile(zipIn, filePath)
            } else {
                // If the entry is a directory, create the directory
                val dir = File(filePath)
                dir.mkdirs()
            }

            zipIn.closeEntry()
            currentEntry = zipIn.nextEntry
        }

        zipIn.close()
    }

    @Throws(IOException::class)
    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(filePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read = 0
        while (zipIn.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }

    private fun unrar(archivePath: String, destPath: String) {
        createUnArchiveDir(destPath)
        Junrar.extract(archivePath, destPath)
    }

    private fun createUnArchiveDir(destPath: String) {
        val destDir = File(destPath)
        if (!destDir.exists()) {
            destDir.mkdir()
        }
    }

    fun onSubtitlesLoaded(downloadsDir: String?) {
        lock.withLock {
            this.downloadsDir = downloadsDir
            subsCheckAndDownload.signal()
        }
    }

    private fun playVideo(filePath: String, filePathWithoutExt: String, isLocal: Boolean) {
        if (MainApplication.appContext.isAppInstalled(VLC_PLAYER_PACKAGE)) {
            val vlcIntent = Intent(Intent.ACTION_VIEW)
            val uri: Uri = if (isLocal) {
                val appContext = MainApplication.appContext
                vlcIntent.putExtra("subtitles_location", "$filePathWithoutExt.srt")
                FileProvider.getUriForFile(appContext, "${appContext.packageName}.provider", File(filePath))
            } else {
                val ipAndPath = rootDirPath.split('@')[1]
                val ip = if (ipAndPath.contains('/')) {
                    ipAndPath.split('/')[0]
                } else {
                    ipAndPath
                }

                lock.withLock {
                    _eventDownloadSubs.postValue(Event("$filePathWithoutExt.srt"))
                    subsCheckAndDownload.await()
                }

                if (downloadsDir != null) {
                    vlcIntent.putExtra("subtitles_location", "$downloadsDir/sub.srt")
                }

                Uri.parse("sftp://$ip$filePath")
            }

            vlcIntent.setPackage(VLC_PLAYER_PACKAGE)
            vlcIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            vlcIntent.setDataAndTypeAndNormalize(uri, "video/*")
            _eventPlayVideo.postValue(Event(vlcIntent))
        } else {
            _eventInstallVLC.postValue(Event(Unit))
        }
    }

    fun onDialogPositiveClick(result: String) {
        Log.d("DMK", "Pass entered: $result")
        _eventPasswordSet.value = Event(result)
    }

    companion object {
        private const val BUFFER_SIZE = 4096
        const val VLC_PLAYER_PACKAGE = "org.videolan.vlc"
        private val videoFormats = setOf("mkv", "avi", "mp4", "3gp", )
    }
}