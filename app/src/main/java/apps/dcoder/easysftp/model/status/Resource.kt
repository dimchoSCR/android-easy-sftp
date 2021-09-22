package apps.dcoder.easysftp.model.status

data class Resource<out T, out K>(val status: Status, val data: T?, val message: String?, val payload: K? = null) {
    companion object {
        fun <T, K> success(data: T?): Resource<T, K> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T, K> success(data: T?, payload: K): Resource<T, K> {
            return Resource(Status.SUCCESS, data, null, payload)
        }

        fun <T, K> error(msg: String, data: T? = null): Resource<T, K> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T, K> loading(): Resource<T, K> {
            return Resource(Status.LOADING, null, null)
        }
    }
}