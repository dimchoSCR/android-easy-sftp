package apps.dcoder.easysftp.filemanager

import androidx.annotation.WorkerThread
import apps.dcoder.easysftp.model.FileInfo
import java.io.Serializable
import java.util.ArrayList
import java.util.LinkedHashMap

interface FileManager: Serializable {
    val rootDirectoryPath: String
    val filesCache: LinkedHashMap<String, List<FileInfo>>

    var currentDir: String

    fun prepare(onPrepared: () -> Unit)
    fun setOnFileManagerResultListener(listener: OnFileManagerResultListener)
    @WorkerThread
    fun listDirectory(dirPath: String, forceRefresh: Boolean = false): List<FileInfo>
    @WorkerThread
    fun listParent(): List<FileInfo>

    fun isOnRootDir(): Boolean {
        return currentDir == rootDirectoryPath
    }

    fun listCurrentDir(forceRefresh: Boolean): List<FileInfo> {
        return listDirectory(currentDir, forceRefresh)
    }

    fun getParentDirectoryPath(dir: String): String
    fun getCurrentlyListedFiles(): List<FileInfo>

    fun putInCache(dirPath: String, files: List<FileInfo>)
    fun getCachedFolder(dirPath: String): List<FileInfo>? {
        currentDir = dirPath
        return filesCache[dirPath]
    }

    fun exit() = filesCache.clear()
}