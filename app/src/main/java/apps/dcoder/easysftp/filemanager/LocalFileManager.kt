package apps.dcoder.easysftp.filemanager

import android.util.Log
import apps.dcoder.easysftp.model.FileInfo
import apps.dcoder.easysftp.model.getFileInfoFromFile
import java.io.File
import java.util.Collections
import kotlin.collections.LinkedHashMap

class LocalFileManager(override val rootDirectoryPath: String): FileManager {

    private var fileManagerResultListener: OnFileManagerResultListener? = null
    override var currentDir: String = rootDirectoryPath

    override val filesCache: LinkedHashMap<String, List<FileInfo>> = linkedMapOf()

    override fun prepare(onPrepared: () -> Unit) = onPrepared()

    override fun setOnFileManagerResultListener(listener: OnFileManagerResultListener) {
        fileManagerResultListener = listener
    }

    override fun listDirectory(dirPath: String, forceRefresh: Boolean): List<FileInfo> {
        currentDir = dirPath
        val cache = filesCache[dirPath]
        if (!forceRefresh && cache != null) {
            Log.wtf(this::class.java.simpleName, "Read from cache")
            return cache
        }

        Log.wtf(this::class.java.simpleName, "No cache")

        val files = mutableListOf<FileInfo>()
        val directory = File(dirPath)
        if (!directory.canRead() && !directory.setReadable(true)) {
            fileManagerResultListener?.onFilesListed()
            return mutableListOf()
        }

        val containedFiles = directory.listFiles()
        if (containedFiles == null) {
            Log.w(this::class.java.simpleName, "No files in directory!")
            return mutableListOf()
        }

        for (file in containedFiles) {
            files.add(getFileInfoFromFile(file))
        }

        Collections.sort(files, AlphaNumericComparator())

        // Insert the listed directory in the cache
        if (files.isNotEmpty()) {
            putInCache(dirPath, files)
        }

        fileManagerResultListener?.onFilesListed()
        return files
    }

    override fun listParent(): List<FileInfo> {
        val parentPath = getParentDirectoryPath(currentDir)
        currentDir = parentPath
        val cache = filesCache[parentPath]
        if (cache != null) {
            return cache
        }

        return listDirectory(parentPath)
    }

    override fun getCurrentlyListedFiles(): List<FileInfo> {
        return filesCache[currentDir] ?: mutableListOf()
    }

    override fun putInCache(dirPath: String, files: List<FileInfo>) {
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

    override fun getParentDirectoryPath(dir: String): String {
        return File(dir).parentFile?.absolutePath
            ?: throw NoSuchFileException(File(dir), null, "Directory $dir, has no parent!")
    }
}