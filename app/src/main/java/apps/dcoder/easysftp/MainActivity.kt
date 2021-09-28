package apps.dcoder.easysftp

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import apps.dcoder.easysftp.broadcast.RemovableMediaStatusBroadcastReceiver
import apps.dcoder.easysftp.filemanager.FileManager
import apps.dcoder.easysftp.services.android.FileManagerService
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val removableMediaStatusBroadcastReceiver: RemovableMediaStatusBroadcastReceiver by inject()

    private val ejectedMediaIntentFilter = IntentFilter()

    companion object {
        private const val INTENT_FILTER_DATA_SCHEME = "file"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        initializeEjectIntentFilter()

        this.registerReceiver(removableMediaStatusBroadcastReceiver, ejectedMediaIntentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(removableMediaStatusBroadcastReceiver)
        if (!isChangingConfigurations) {
            Log.d("DMK", "Stopping service")
            stopService(Intent(this, FileManagerService::class.java))
        }
    }

    private fun initializeEjectIntentFilter() {
        ejectedMediaIntentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED)
        ejectedMediaIntentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED)
        ejectedMediaIntentFilter.addDataScheme(INTENT_FILTER_DATA_SCHEME);
    }

}
