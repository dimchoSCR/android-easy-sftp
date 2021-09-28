package apps.dcoder.easysftp.extensions

import android.app.ActivityManager

fun ActivityManager.isServiceRunning(serviceClass: Class<*>): Boolean {
    for (serviceInfo in this.getRunningServices(10)) {
        if (serviceInfo.started && serviceInfo.service.className == serviceClass.name) {
            return true
        }
    }

    return false
}