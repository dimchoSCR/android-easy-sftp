package apps.dcoder.easysftp.util

import java.util.concurrent.atomic.AtomicBoolean

open class Event<T>(private val value: T?) {
    private val consumed: AtomicBoolean = AtomicBoolean(false)

    fun consume(consumerEvent: (data: T?) -> Unit) {
        if (consumed.compareAndSet(false, true)) {
            consumerEvent(value)
        }
    }
}