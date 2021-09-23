package apps.dcoder.easysftp.util

import apps.dcoder.easysftp.extensions.executeOnMainDispatcher
import apps.dcoder.easysftp.model.status.Resource
import kotlinx.coroutines.Dispatchers

open class MutableLiveResource<T : Any?, K: Any?> : LiveResource<T, K> {
    constructor()
    constructor(value: Resource<T, K>) : super(value)

    // Standard functions
    fun setLoading() {
        value = Resource.loading()
    }

    fun setSuccess(data: T) {
        value = Resource.success(data)
    }

    fun setSuccess(data: T, payload: K) {
        value = Resource.success(data, payload)
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

    suspend fun dispatchSuccessOnMain(data: T, payload: K) = Dispatchers.executeOnMainDispatcher {
        setSuccess(data, payload)
    }

    suspend fun dispatchErrorOnMain(data: T, errMsg: String) = Dispatchers.executeOnMainDispatcher {
        setError(data, errMsg)
    }

    suspend fun dispatchErrorOnMain(errMsg: String) = Dispatchers.executeOnMainDispatcher {
        setError(errMsg)
    }
}