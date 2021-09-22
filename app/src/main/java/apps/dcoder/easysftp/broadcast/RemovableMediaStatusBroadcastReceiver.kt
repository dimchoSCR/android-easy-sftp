package apps.dcoder.easysftp.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import apps.dcoder.easysftp.services.storage.RemovableMediaState
import apps.dcoder.easysftp.services.storage.StorageDiscoveryService

class RemovableMediaStatusBroadcastReceiver(
    private val storageDiscoveryService: StorageDiscoveryService
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val pathToMedia = intent.dataString

            if (pathToMedia == null || pathToMedia.isEmpty()) {
                Log.e(this.javaClass.simpleName, "Received mount status for unknown removable media!")
                return
            }

            val mediaState = when (intent.action) {
                Intent.ACTION_MEDIA_UNMOUNTED -> RemovableMediaState.UNMOUNTED

                Intent.ACTION_MEDIA_MOUNTED -> RemovableMediaState.MOUNTED

                else -> {
                    Log.e(this.javaClass.simpleName, "Unsupported broadcast!")
                    return@let
                }
            }

            storageDiscoveryService.notifyRemovableMediaSateChange(pathToMedia, mediaState)
        }
    }
}