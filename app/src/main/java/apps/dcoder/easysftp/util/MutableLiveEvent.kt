package apps.dcoder.easysftp.util

class MutableLiveEvent<T> : LiveEvent<T> {
    constructor()
    constructor(value: Event<T>?) : super(value)

    public override fun setValue(value: Event<T>?) {
        super.setValue(value)
    }

    public override fun postValue(value: Event<T>?) {
        super.postValue(value)
    }
}