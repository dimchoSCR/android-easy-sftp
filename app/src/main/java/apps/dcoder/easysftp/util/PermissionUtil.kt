package apps.dcoder.easysftp.util

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import apps.dcoder.easysftp.MainApplication

object PermissionUtil {
    fun askPermissionIfNotGranted(
        requestPermLauncher: ActivityResultLauncher<String>,
        permission: String,
        onPermissionAlreadyGranted: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                MainApplication.appContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermLauncher.launch(permission)
        } else {
            // Permission has already been granted
            onPermissionAlreadyGranted()
        }
    }
}