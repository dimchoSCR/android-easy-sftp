package apps.dcoder.easysftp.filemanager.remote

import android.util.Log
import com.jcraft.jsch.SftpProgressMonitor

interface FileOperationStatusListener {
    fun onOpStarted()
    fun onUpdateOpProgress(bytesTransferred: Long, totalBytes: Long)
    fun onOpComplete()
}

class SftpProgressMonitor(private val totalBytes: Long = 0) : SftpProgressMonitor {
    private var totalSize = 0L
    private var bytesCopied = 0L

    var sftpOpListener: FileOperationStatusListener? = null

    override fun init(op: Int, src: String?, dest: String?, max: Long) {
        totalSize = if (max != -1L) {
            max
        } else {
            totalBytes
        }

        sftpOpListener?.onOpStarted()
        Log.d("Copy", "Copying $max bytes from: $src, to: $dest")
    }

    override fun count(bytes: Long): Boolean {
        bytesCopied += bytes
        sftpOpListener?.onUpdateOpProgress(bytesCopied, totalSize)

        return true
    }

    override fun end() {
        Log.d("Copy", "Copy finished")
        sftpOpListener?.onOpComplete()
    }
}