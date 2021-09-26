package apps.dcoder.easysftp.filemanager

import apps.dcoder.easysftp.model.FileInfo
import java.io.Serializable
import java.util.ArrayList
import java.util.LinkedHashMap

interface FileManager: Serializable {
    val rootDirectoryPath: String
    val filesCache: LinkedHashMap<String, List<FileInfo>>

    fun prepare(onPrepared: (fm: FileManager) -> Unit)
    fun setOnFileManagerResultListener(listener: OnFileManagerResultListener)
    fun listDirectory(dirPath: String)
    fun getParentDirectoryPath(dir: String): String
    fun getCurrentlyListedFiles(): ArrayList<FileInfo>

    fun useCachedFolder(dirPath: String): Boolean {
        val cachedDir: List<FileInfo> = filesCache[dirPath] ?: return false

        val files = getCurrentlyListedFiles()
        files.clear()
        files.addAll(cachedDir)

        return true
    }

    fun clearChildrenFromCache(parentPath: String) {

        val parentDirList = filesCache[parentPath] ?:
                        throw NoSuchElementException("Parent directory can not be found in cache!")

        // Clear the cache
        filesCache.clear()

        // Caches the parentList by copying it
        // This way only the child files are cleared
        // and listing the contents of the root directory is avoided
        filesCache[rootDirectoryPath] = parentDirList
    }

    fun exit() = filesCache.clear()
}