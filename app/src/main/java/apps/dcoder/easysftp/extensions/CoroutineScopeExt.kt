package apps.dcoder.easysftp.extensions

import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

inline fun CoroutineScope.launchCancellable(
    dispatcher: CoroutineContext = Dispatchers.IO,
    crossinline block: suspend CoroutineScope.() -> Unit
): Job = launch {
    val deferred = this.async(dispatcher) {
        block()
    }

    deferred.await()
}