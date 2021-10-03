package apps.dcoder.easysftp.fragments.dialog

interface DialogActionListener<T> {
    fun onDialogPositiveClick(result: T)
    fun onDialogNegativeClick() { }
}