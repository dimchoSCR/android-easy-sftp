package apps.dcoder.easysftp.filemanager

import android.util.Log
import apps.dcoder.easysftp.model.FileInfo
import apps.dcoder.easysftp.model.getFileInfoFromSftp
import com.jcraft.jsch.*
import com.jcraft.jsch.ChannelSftp.LsEntrySelector
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

class RemoteFileManager(fullyQualifiedPath: String) : FileManager {

    override val rootDirectoryPath: String
    override var currentDir: String = ""
    override val filesCache: LinkedHashMap<String, List<FileInfo>> = linkedMapOf()

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
                initConnection()
            }
            onPrepared()
        }
    }

    override fun setOnFileManagerResultListener(listener: OnFileManagerResultListener) = Unit

    override fun listDirectory(dirPath: String, forceRefresh: Boolean): List<FileInfo> {
        currentDir = dirPath

        val converted: MutableList<FileInfo> = ArrayList(100)
        val selector = LsEntrySelector { entry ->
            val fileName = entry.filename
            if (!fileName.matches(Regex("\\.+"))) {
                converted.add(getFileInfoFromSftp(entry, dirPath))
            }
            LsEntrySelector.CONTINUE
        }

        sftpChannel.ls(dirPath, selector)

        // TODO use comparator from preferences preferences
        Collections.sort(converted, AlphaNumericComparator())

        // Insert the listed directory in the cache
        if (converted.isNotEmpty()) {
            putInCache(dirPath, converted)
        }

        return converted
    }

    override fun getParentDirectoryPath(dir: String): String {
        return dir.substring(0, dir.lastIndexOf(File.separatorChar))
    }

    override fun getCurrentlyListedFiles(): List<FileInfo> {
        return filesCache[currentDir] ?: mutableListOf()
    }

    override fun exit() {
        super.exit()
        executor.shutdown()
    }

}