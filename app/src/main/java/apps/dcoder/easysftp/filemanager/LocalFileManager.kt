package apps.dcoder.easysftp.filemanager

import android.util.Log
import apps.dcoder.easysftp.model.FileInfo
import apps.dcoder.easysftp.model.getFileInfoFromFile
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class LocalFileManager(override val rootDirectoryPath: String): FileManager {

    private val files: java.util.ArrayList<FileInfo> = java.util.ArrayList(30)

    private lateinit var fileManagerResultListener: OnFileManagerResultListener

//    override val rootDirectoryPath: String
//        get() = Environment.getExternalStorageDirectory().absolutePath

    override val filesCache: LinkedHashMap<String, List<FileInfo>> = linkedMapOf()

//    override var shouldReInitiateDirListing: Boolean = false

    override fun prepare(onPrepared: (fm: FileManager) -> Unit) {
        onPrepared(this)
    }

    override fun setOnFileManagerResultListener(listener: OnFileManagerResultListener) {
        fileManagerResultListener = listener
    }

    override fun listDirectory(dirPath: String) {
        files.clear()

        val directory = File(dirPath)
        if (!directory.canRead() && !directory.setReadable(true)) {
            fileManagerResultListener.onFilesListed()
        }

        val containedFiles = directory.listFiles()
        if (containedFiles == null) {
            Log.w(this::class.java.simpleName, "No files in directory!")
            return
        }

        for (file in containedFiles) {
            files.add(getFileInfoFromFile(file))
        }

        Collections.sort(files, AlphaNumericComparator())

        // Insert the listed directory in the cache
        if(files.isNotEmpty()) {
            filesCache[dirPath] = ArrayList(files)
        }

        fileManagerResultListener.onFilesListed()
    }

    override fun getCurrentlyListedFiles(): java.util.ArrayList<FileInfo> {
        return files
    }

    override fun getParentDirectoryPath(dir: String): String {
        return File(dir).parentFile?.absolutePath ?: throw NoSuchFileException(File(dir), null, "Directory $dir, has no parent!")
    }
}