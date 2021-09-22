package apps.dcoder.easysftp.extensions

import androidx.lifecycle.MutableLiveData
import apps.dcoder.easysftp.model.status.Resource
import kotlinx.coroutines.Dispatchers

fun<T : Any?, K: Any?> MutableLiveData<Resource<T, K>>.setLoading() {
    value = Resource.loading()
}

suspend inline fun<T : Any?, K : Any?> MutableLiveData<Resource<T, K>>.dispatchLoadingOnMain() = Dispatchers.executeOnMainDispatcher {
    setLoading()
}

fun<T : Any?, K : Any?> MutableLiveData<Resource<T, K>>.setSuccess(data: T) {
    value = Resource.success(data)
}

fun<T : Any?, K : Any> MutableLiveData<Resource<T, K>>.setSuccess(data: T, payload: K) {
    value = Resource.success(data, payload)
}

suspend inline fun<T : Any?, K : Any> MutableLiveData<Resource<T, K>>.dispatchSuccessOnMain(data: T) = Dispatchers.executeOnMainDispatcher {
    setSuccess(data)
}

suspend inline fun<T : Any?, K : Any> MutableLiveData<Resource<T, K>>.dispatchSuccessOnMain(data: T, payload: K) = Dispatchers.executeOnMainDispatcher {
    setSuccess(data, payload)
}

fun<T : Any?, K : Any?> MutableLiveData<Resource<T, K>>.setError(data: T, errMsg: String) {
    value = Resource.error(errMsg, data)
}

fun<T : Any?, K : Any?> MutableLiveData<Resource<T, K>>.setError(errMsg: String) {
    value = Resource.error(errMsg)
}

suspend inline fun<T : Any?, K : Any?> MutableLiveData<Resource<T, K>>.dispatchErrorOnMain(data: T, errMsg: String) = Dispatchers.executeOnMainDispatcher {
    setError(data, errMsg)
}

suspend inline fun<T : Any?, K : Any?> MutableLiveData<Resource<T, K>>.dispatchErrorOnMain(errMsg: String) = Dispatchers.executeOnMainDispatcher {
    setError(errMsg)
}



