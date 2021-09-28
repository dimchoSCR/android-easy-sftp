package apps.dcoder.easysftp.util

import apps.dcoder.easysftp.extensions.executeOnMainDispatcher
import kotlinx.coroutines.Dispatchers

open class MutableLiveResource<T : Any?> : LiveResource<T> {
    constructor()
    constructor(value: Resource<T>) : super(value)

    // Standard functions
    fun setLoading() {
        value = Resource.loading()
    }

    fun setSuccess(data: T) {
        value = Resource.success(data)
    }

    fun setError(data: T, errMsg: String) {
        value = Resource.error(errMsg, data)
    }

    fun setError(errMsg: String) {
        value = Resource.error(errMsg)
    }

    // Suspending functions
    suspend inline fun dispatchLoadingOnMain() = Dispatchers.executeOnMainDispatcher {
        setLoading()
    }

    suspend fun dispatchSuccessOnMain(data: T) = Dispatchers.executeOnMainDispatcher {
        setSuccess(data)
    }

    suspend fun dispatchErrorOnMain(data: T, errMsg: String) = Dispatchers.executeOnMainDispatcher {
        setError(data, errMsg)
    }

    suspend fun dispatchErrorOnMain(errMsg: String) = Dispatchers.executeOnMainDispatcher {
        setError(errMsg)
    }
}