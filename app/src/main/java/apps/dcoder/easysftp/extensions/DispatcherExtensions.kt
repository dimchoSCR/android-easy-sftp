package apps.dcoder.easysftp.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun Dispatchers.executeOnMainDispatcher(doOnMainDispatcher: () -> Unit) = withContext(Dispatchers.Main) {
    doOnMainDispatcher()
}