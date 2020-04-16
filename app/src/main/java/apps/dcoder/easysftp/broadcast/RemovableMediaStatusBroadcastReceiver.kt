package apps.dcoder.easysftp.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RemovableMediaStatusBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val pathToMedia = intent.dataString

            when (intent.action) {
                Intent.ACTION_MEDIA_UNMOUNTED -> {
                    Log.d(this.javaClass.simpleName, "Unmounted media path: $pathToMedia")
                }

                Intent.ACTION_MEDIA_MOUNTED -> {
                    Log.d(this.javaClass.simpleName, "Mounted media path: $pathToMedia")
                }

                else -> Log.e(this.javaClass.simpleName, "Unsupported broadcast!")
            }
        }
    }
}