package apps.dcoder.easysftp.util

import apps.dcoder.easysftp.util.status.Status
import java.util.concurrent.atomic.AtomicBoolean

data class Resource<T>(val status: Status, val data: T?, val message: String?)  {
    private val consumed: AtomicBoolean = AtomicBoolean(false)

    fun consume(consumerEvent: (Resource<T>) -> Unit) {
        if (consumed.compareAndSet(false, true)) {
           consumerEvent(this)
        }
    }

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING, null, null)
        }
    }
}