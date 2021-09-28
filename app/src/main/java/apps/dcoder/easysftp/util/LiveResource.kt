package apps.dcoder.easysftp.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

open class LiveResource<T> : LiveData<Resource<T>> {
    constructor()
    constructor(value: Resource<T>?) : super(value)

    class EventResourceObserver<T>(private val consumerEvent: (Resource<T>) -> Unit) : Observer<Resource<T>> {
        override fun onChanged(t: Resource<T>) {
            t.consume(consumerEvent)
        }
    }

    fun consume(lifecycleOwner: LifecycleOwner, consumerEvent: (Resource<T>) -> Unit) {
        observe(lifecycleOwner, EventResourceObserver(consumerEvent))
    }
}