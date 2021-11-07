package apps.dcoder.easysftp.filemanager

import androidx.annotation.WorkerThread
import apps.dcoder.easysftp.filemanager.remote.FileOperationStatusListener
import apps.dcoder.easysftp.model.FileInfo
import java.io.InputStream
import java.io.Serializable
import java.lang.IllegalStateException
import java.util.LinkedHashMap

interface FileManager: Serializable {
    var fileOpListener: FileOperationStatusListener?

    val rootDirectoryPath: String
    val filesCache: LinkedHashMap<String, List<FileInfo>>

    var currentDir: String

    fun prepare(onPrepared: () -> Unit)
    fun setOnFileManagerResultListener(listener: OnFileManagerResultListener)
    @WorkerThread
    fun listDirectory(dirPath: String, forceRefresh: Boolean = false): List<FileInfo>
    @WorkerThread
    fun listParent(): List<FileInfo> {
        val parentPath = getParentDirectoryPath(currentDir)
        currentDir = parentPath
        val cache = filesCache[parentPath]
        if (cache != null) {
            return cache
        }

        return listDirectory(parentPath)
    }

    fun onCurrentOperationCancelled()

    fun isOnRootDir(): Boolean {
        return currentDir == rootDirectoryPath
    }

    fun listCurrentDir(forceRefresh: Boolean): List<FileInfo> {
        return listDirectory(currentDir, forceRefresh)
    }

    fun getParentDirectoryPath(dir: String): String
    fun getCurrentlyListedFiles(): List<FileInfo>

    fun putInCache(dirPath: String, files: List<FileInfo>) {
        if (filesCache.entries.size <= 10) {
            filesCache[dirPath] = files
        } else {
            var keyToRemove = ""
            for ((i, entry) in filesCache.entries.withIndex()) {
                if (i == filesCache.entries.size / 2) {
                    keyToRemove = entry.key
                    break
                }
            }

            if (keyToRemove != "") {
                filesCache.remove(keyToRemove)
            }

            filesCache[dirPath] = files
        }
    }

    fun getCachedFolder(dirPath: String): List<FileInfo>? {
        currentDir = dirPath
        return filesCache[dirPath]
    }

    fun removeFileFromCache(fileName: String, dirPath: String) {
        val files = filesCache[dirPath] ?: throw IllegalStateException("Directory is not in cache!")
        filesCache.remove(dirPath)
        val mutableFilesCache = files.toMutableList()
        var indexOfRenamedFile = -1
        // Remove file from cache
        for (i in mutableFilesCache.indices) {
            if (mutableFilesCache[i].name == fileName) {
                indexOfRenamedFile = i
            }
        }

        mutableFilesCache.removeAt(indexOfRenamedFile)
        putInCache(dirPath, mutableFilesCache)
    }

    fun removeDirFromCache(dirPath: String) {
        filesCache.remove(dirPath)
    }

    fun getInputStreamWithSize(sourceFilePath: String): Pair<InputStream, Long>

    fun paste(clipBoardEntry: ClipBoardManager.ClipBoardEntry, destinationDir: String = currentDir)

    fun rename(oldName: String, newName: String): FileInfo

    fun exists(path: String): Boolean

    fun delete(filePath: String)

    fun exit() = filesCache.clear()
}