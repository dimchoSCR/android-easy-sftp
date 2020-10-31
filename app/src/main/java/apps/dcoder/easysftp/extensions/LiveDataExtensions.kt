package apps.dcoder.easysftp.extensions

import androidx.lifecycle.MutableLiveData
import apps.dcoder.easysftp.model.status.Resource
import kotlinx.coroutines.Dispatchers

fun<T : Any?> MutableLiveData<Resource<T>>.setLoading() {
    value = Resource.loading()
}

suspend inline fun<T : Any?> MutableLiveData<Resource<T>>.dispatchLoadingOnMain() = Dispatchers.executeOnMainDispatcher {
    setLoading()
}

fun<T : Any?> MutableLiveData<Resource<T>>.setSuccess(data: T) {
    value = Resource.success(data)
}

suspend inline fun<T : Any?> MutableLiveData<Resource<T>>.dispatchSuccessOnMain(data: T) = Dispatchers.executeOnMainDispatcher {
    setSuccess(data)
}

fun<T : Any?> MutableLiveData<Resource<T>>.setError(data: T, errMsg: String) {
    value = Resource.error(errMsg, data)
}

suspend inline fun<T : Any?> MutableLiveData<Resource<T>>.dispatchErrorOnMain(data: T, errMsg: String) = Dispatchers.executeOnMainDispatcher {
    setError(data, errMsg)
}



