package apps.dcoder.easysftp.filemanager.remote

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import apps.dcoder.easysftp.MainApplication
import com.jcraft.jsch.UserInfo
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SftpUserInfo : UserInfo {
    private var sshPassword: String? = null
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    var onPasswordRequested: (() -> Unit)? = null

    override fun promptPassphrase(message: String?): Boolean {
        return false
    }

    override fun getPassphrase(): String? {
        return null
    }

    override fun getPassword(): String? {
        return sshPassword
    }

    override fun promptYesNo(message: String?): Boolean {
        return true
    }

    override fun showMessage(message: String?) {}

    override fun promptPassword(message: String?): Boolean {
        onPasswordRequested?.invoke()

        lock.withLock {
            condition.await()
            Log.d("DMK", "Password set! $password")

            return true
        }
    }

    fun setPassword(sshPassword: String) {
        this.sshPassword = sshPassword
        lock.withLock {
            condition.signal()
        }
    }
}