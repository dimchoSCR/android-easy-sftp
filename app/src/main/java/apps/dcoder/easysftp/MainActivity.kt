package apps.dcoder.easysftp

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import apps.dcoder.easysftp.broadcast.RemovableMediaStatusBroadcastReceiver
import apps.dcoder.easysftp.services.android.FileManagerService
import com.google.android.material.appbar.AppBarLayout
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

        val appBar: AppBarLayout = findViewById(R.id.app_bar)
        val appbarTopPadding = appBar.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { v, insets ->
            v.updatePadding(top = appbarTopPadding +
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
            insets
        }

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
