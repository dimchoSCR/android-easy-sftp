package apps.dcoder.easysftp.filemanager.local

import android.util.Log
import apps.dcoder.easysftp.filemanager.AlphaNumericComparator
import apps.dcoder.easysftp.filemanager.FileManager
import apps.dcoder.easysftp.filemanager.OnFileManagerResultListener
import apps.dcoder.easysftp.filemanager.remote.FileOperationStatusListener
import apps.dcoder.easysftp.model.FileInfo
import apps.dcoder.easysftp.model.getFileInfoFromFile
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.IllegalStateException
import java.util.Collections
import kotlin.collections.LinkedHashMap

class LocalFileManager(override var rootDirectoryPath: String): FileManager {

    private var fileManagerResultListener: OnFileManagerResultListener? = null

    override var currentDir: String = rootDirectoryPath
    override var fileOpListener: FileOperationStatusListener? = null

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

    override fun onCurrentOperationCancelled() = Unit

    override fun getCurrentlyListedFiles(): List<FileInfo> {
        return filesCache[currentDir] ?: mutableListOf()
    }

    override fun getInputStream(sourceFilePath: String): InputStream {
        return FileInputStream(sourceFilePath)
    }

    override fun paste(sourceFilePath: String, destFileName: String, destinationDir: String) = Unit

    override fun rename(oldName: String, newName: String): FileInfo {
        val resultFile = File(currentDir, newName)
        removeFileFromCache(oldName, currentDir)

        File(currentDir, oldName).renameTo(resultFile)

        val resultFileInfo = getFileInfoFromFile(resultFile)
        val files = filesCache[currentDir] ?: throw IllegalStateException("Directory is not in cache!")
        val mutableFiles = files.toMutableList()

        mutableFiles.add(resultFileInfo)
        Collections.sort(mutableFiles, AlphaNumericComparator())
        putInCache(currentDir, mutableFiles)

        return resultFileInfo
    }

    override fun delete(filePath: String) {
        val fileToDelete = File(filePath)
        if (fileToDelete.isDirectory) {
            deleteDirContents(filePath)
            // TODO remove children from cache as well
            fileToDelete.delete()
        } else {
            fileToDelete.delete()
        }

        val fileName = filePath.substring(filePath.lastIndexOf("/") + 1)
        removeFileFromCache(fileName, currentDir)
    }

    private fun deleteDirContents(dirPath: String) {
        val fileToDelete = File(dirPath)
        val dirFiles = fileToDelete.listFiles()
        if (dirFiles != null) {
            for (file in dirFiles) {
                if (file.isDirectory) {
                    deleteDirContents(file.absolutePath)
                } else {
                    file.delete()
                }
            }
        }

        fileToDelete.delete()
    }

    override fun getParentDirectoryPath(dir: String): String {
        return File(dir).parentFile?.absolutePath
            ?: throw NoSuchFileException(File(dir), null, "Directory $dir, has no parent!")
    }

    fun changeLocalRootDir(newRoot: String) {
        rootDirectoryPath = newRoot
    }
}