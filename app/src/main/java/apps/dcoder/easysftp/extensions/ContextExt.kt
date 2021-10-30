package apps.dcoder.easysftp.extensions

import android.content.Context
import android.content.pm.PackageManager

fun Context.isAppInstalled(packageName: String): Boolean {
    return try {
        this.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (err: PackageManager.NameNotFoundException) {
        false
    }
}