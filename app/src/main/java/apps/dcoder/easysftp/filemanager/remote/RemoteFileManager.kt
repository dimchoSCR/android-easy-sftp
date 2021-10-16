package apps.dcoder.easysftp.filemanager.remote

import android.util.Log
import apps.dcoder.easysftp.filemanager.AlphaNumericComparator
import apps.dcoder.easysftp.filemanager.FileManager
import apps.dcoder.easysftp.filemanager.OnFileManagerResultListener
import apps.dcoder.easysftp.model.FileInfo
import apps.dcoder.easysftp.model.getFileInfoFromSftp
import com.jcraft.jsch.*
import com.jcraft.jsch.ChannelSftp.LsEntrySelector
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import java.util.regex.Pattern
import kotlin.concurrent.withLock

class RemoteFileManager(fullyQualifiedPath: String) : FileManager {
    override var fileOpListener: FileOperationStatusListener? = null
    override var currentDir: String = ""

    override val rootDirectoryPath: String
    override val filesCache: LinkedHashMap<String, List<FileInfo>> = linkedMapOf()

    private val lock = ReentrantLock()
    private val username: String
    private val ip: String

    private var isConnected = false

    init {
        val userNameAndAddressArr = fullyQualifiedPath.split('@')
        if (userNameAndAddressArr.size != 2) {
            throw Exception("Fully qualified path: $fullyQualifiedPath, was not valid!")
        }

        username = userNameAndAddressArr[0]

        val ipAndFolderPath = userNameAndAddressArr[1].split(Pattern.compile("/"), 2)
        if (ipAndFolderPath.size != 2) {
            throw Exception("Fully qualified path: $fullyQualifiedPath, does not contain ip and folder path!")
        }

        ip = ipAndFolderPath[0]
        rootDirectoryPath = "/" + ipAndFolderPath[1]
        currentDir = rootDirectoryPath
    }

    private lateinit var executor: ExecutorService

    private lateinit var session : Session
    private lateinit var sftpChannel: ChannelSftp

    var onPasswordRequested: (() -> Unit)? = null
        set(value) {
            field = value
            userInfo.onPasswordRequested = value
        }

    private val userInfo = SftpUserInfo()

    fun setSshPassword(password: String) {
        Log.d("DMK", "Pass in remote file manager: $password")
        userInfo.setPassword(password)
    }

    private fun initConnection()  {
        try {
            session = JSch().getSession(username, ip)
            session.userInfo = userInfo

            session.connect()
            Log.d("DMK", "CONNECTED")

            val channel: Channel = session.openChannel("sftp")
            channel.connect()

            sftpChannel = channel as ChannelSftp
            isConnected = true
        } catch (err: Exception) {
            Log.e("DMK", "Error while initialising session", err)
        }
    }

    override fun prepare(onPrepared: () -> Unit) {
        executor = Executors.newSingleThreadExecutor()
        executor.execute {
            if (!isConnected) {
                try {
                    initConnection()
                } catch (err: Exception) {
                    Log.e("DMK", "Error while initialising session", err)
                    return@execute
                }
            }
            onPrepared()
        }
    }

    override fun setOnFileManagerResultListener(listener: OnFileManagerResultListener) = Unit

    override fun listDirectory(dirPath: String, forceRefresh: Boolean): List<FileInfo> {
        currentDir = dirPath
        val cache = filesCache[dirPath]
        if (!forceRefresh && cache != null) {
            Log.wtf(this::class.java.simpleName, "Read from cache")
            return cache
        }

        val converted: MutableList<FileInfo> = ArrayList(100)
        val selector = LsEntrySelector { entry ->
            val fileName = entry.filename
            if (!fileName.matches(Regex("\\.+"))) {
                converted.add(getFileInfoFromSftp(entry, dirPath))
            }
            LsEntrySelector.CONTINUE
        }

        try {
            // Run op only when there is no cancel in progress
            lock.withLock {
                sftpChannel.ls(dirPath, selector)
            }
        } catch (err: Exception) {
            Log.e(this::class.java.simpleName, "Exception occurred while listing files", err)
            return emptyList()
        }

        // TODO use comparator from preferences preferences
        Collections.sort(converted, AlphaNumericComparator())

        // Insert the listed directory in the cache
        if (converted.isNotEmpty()) {
            putInCache(dirPath, converted)
        }

        return converted
    }

    override fun onCurrentOperationCancelled() {
        Log.d("DMK", "Op cancelled")
        // Block any new operations from running
        lock.withLock {
            if (::sftpChannel.isInitialized) {
                sftpChannel.exit()
            }

            val newChannel = session.openChannel("sftp")
            newChannel.connect()

            sftpChannel = newChannel as ChannelSftp
        }
    }

    override fun getParentDirectoryPath(dir: String): String {
        return dir.substring(0, dir.lastIndexOf(File.separatorChar))
    }

    override fun getCurrentlyListedFiles(): List<FileInfo> {
        return filesCache[currentDir] ?: mutableListOf()
    }

    override fun getInputStream(sourceFilePath: String): InputStream {
        return sftpChannel.get(sourceFilePath)
    }

    override fun paste(sourceFilePath: String, destFileName: String, destinationDir: String) {
        val destFile = "$destinationDir/$destFileName"
        val progressMonitor = SftpProgressMonitor()
        progressMonitor.sftpOpListener = this.fileOpListener

        lock.withLock {
            sftpChannel.put(sourceFilePath, destFile, progressMonitor)
        }
    }

    override fun rename(oldName: String, newName: String): FileInfo {
        removeFileFromCache(oldName, currentDir)

        lock.withLock {
            val oldPath = "$currentDir${File.separatorChar}$oldName"
            val newPath = "$currentDir${File.separatorChar}$newName"

            sftpChannel.rename(oldPath, newPath)

            var renamedFileInfo: FileInfo? = null
            val selector = LsEntrySelector { entry ->
                val fileName = entry.filename
                if (fileName == newName) {
                    renamedFileInfo = getFileInfoFromSftp(entry, currentDir)
                    return@LsEntrySelector LsEntrySelector.BREAK
                }
                LsEntrySelector.CONTINUE
            }

            sftpChannel.ls(currentDir, selector)

            val files = filesCache[currentDir] ?: throw IllegalStateException("Directory is not in cache!")
            val mutableFiles = files.toMutableList()
            val fileInfo = renamedFileInfo ?: throw FileNotFoundException(newPath)
            mutableFiles.add(fileInfo)
            // Sort files again and update cache
            Collections.sort(mutableFiles, AlphaNumericComparator())
            putInCache(currentDir, mutableFiles)

            return fileInfo
        }
    }

    override fun delete(filePath: String) {
        lock.withLock {
            val lsStat = sftpChannel.lstat(filePath)
            if (lsStat.isDir) {
                deleteDirContents(filePath)
            } else {
                sftpChannel.rm(filePath)
            }

            val fileName = filePath.substring(filePath.lastIndexOf("/") + 1)
            removeFileFromCache(fileName, currentDir)
        }
    }

    private fun deleteDirContents(dirPath: String) {
        val converted: MutableList<FileInfo> = ArrayList(100)
        val selector = LsEntrySelector { entry ->
            val fileName = entry.filename
            if (!fileName.matches(Regex("\\.+"))) {
                converted.add(getFileInfoFromSftp(entry, dirPath))
            }
            LsEntrySelector.CONTINUE
        }

        try {
            sftpChannel.ls(dirPath, selector)
        } catch (err: Exception) {
            Log.e("DMK", "No read permisison for directory: $dirPath")
            return
        }

        for (file in converted) {
            if (file.isDirectory) {
                deleteDirContents(file.absolutePath)
            } else {
                sftpChannel.rm(file.absolutePath)
            }
        }

        sftpChannel.rmdir(dirPath)
    }

    override fun exit() {
        super.exit()
        sftpChannel.quit()
        session.disconnect()
        executor.shutdown()
    }

}