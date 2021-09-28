package apps.dcoder.easysftp.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

open class LiveEvent<T> : LiveData<Event<T>> {

    constructor()
    constructor(value: Event<T>?) : super(value)

    class EventObserver<T>(private val consumerEvent: (data: T?) -> Unit) : Observer<Event<T>> {
        override fun onChanged(t: Event<T>) {
            t.consume(consumerEvent)
        }
    }

    fun consume(lifecycleOwner: LifecycleOwner, consumerEvent: (data: T?) -> Unit) {
        observe(lifecycleOwner, EventObserver(consumerEvent))
    }
}