package apps.dcoder.easysftp.util

import androidx.lifecycle.LiveData
import apps.dcoder.easysftp.model.status.Resource

open class LiveResource<T: Any?, K: Any?> : LiveData<Resource<T, K>> {
    constructor()
    constructor(value: Resource<T, K>?) : super(value)
}