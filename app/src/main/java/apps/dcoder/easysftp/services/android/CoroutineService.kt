import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

abstract class CoroutineService : Service(), CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main

    /**
     * Do the the service work and don't worry if the service isRunning or if it needs to be stopped
     * The stopping logic and the running status is managed here by onStartCommand
     */
    protected abstract fun onStartService(intent: Intent?, flags: Int, startId: Int): Int

    protected fun<T : Service> getStopSelfIntent(serviceClass: Class<T>): PendingIntent = PendingIntent.getService(
        this,
        0,
        Intent(this, serviceClass),
        PendingIntent.FLAG_CANCEL_CURRENT
    )

    var isRunning: Boolean = false
        private set

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            return onStartService(intent, flags, startId)
        } else {
            intent?.let {
                if (intent.flags and PendingIntent.FLAG_CANCEL_CURRENT != 0) {
                    stopSelf()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel coroutine scope to avoid leaks
        this.cancel()
        isRunning = false
    }
}