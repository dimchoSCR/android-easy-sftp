package apps.dcoder.easysftp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import apps.dcoder.easysftp.fragments.dialog.DialogActionListener
import apps.dcoder.easysftp.util.*
import com.github.junrar.Archive
import com.github.junrar.Junrar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.Stack
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

enum class ProgressState {
    LOADING, IDLE
}
class FileViewViewModel(rootDirPath: String) : ViewModel(), DialogActionListener<String> {

    private var prevItemPos = 0
    private var prevScrollOffset = 0
    private var indexOfRenamedFile = -1

    private val _progressState = MutableLiveEvent(Event(ProgressState.LOADING))
    val progressState: LiveEvent<ProgressState> = _progressState

    private val _eventPasswordSet = MutableLiveEvent<String>()
    val eventPasswordSet: LiveEvent<String> = _eventPasswordSet

    private val _archiveExtractionEvent = MutableLiveResource<Unit>()
    val archiveExtractionEvent: LiveResource<Unit> = _archiveExtractionEvent

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
        val positionPair = positionStack.pop()
        val (prevItemPos, prevScrollOffset) = positionPair

        this.prevItemPos = prevItemPos
        this.prevScrollOffset = prevScrollOffset
    }

    fun getSavedScrollPositions(): Pair<Int, Int> {
        return Pair(prevItemPos, prevScrollOffset)
    }

    fun resetRvPositions() {
        prevItemPos = 0
        prevScrollOffset = 0
    }

    fun extractIfArchive(filePath: String, fileName: String) = viewModelScope.launch(Dispatchers.IO) {
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
                _archiveExtractionEvent.dispatchLoadingOnMain()
                unzip(filePath, filePathWithoutExt)
                _archiveExtractionEvent.dispatchSuccessOnMain(Unit)
            }

            "rar" -> {
                _archiveExtractionEvent.dispatchLoadingOnMain()
                unrar(filePath, filePathWithoutExt)
                _archiveExtractionEvent.dispatchSuccessOnMain(Unit)
            }

            else -> {
                Log.d("DMK", "Not a zip nor rar")
            }
        }
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

    override fun onDialogPositiveClick(result: String) {
        Log.d("DMK", "Pass entered: $result")
        _eventPasswordSet.value = Event(result)
    }

    companion object {
        private const val BUFFER_SIZE = 4096
    }
}