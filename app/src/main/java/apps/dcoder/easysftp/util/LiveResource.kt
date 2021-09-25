package apps.dcoder.easysftp.util

import androidx.lifecycle.LiveData
import apps.dcoder.easysftp.model.status.Resource

open class LiveResource<T: Any?> : LiveData<Resource<T>> {
    constructor()
    constructor(value: Resource<T>?) : super(value)
}