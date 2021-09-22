package apps.dcoder.easysftp.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.custom.LabeledEditText
import apps.dcoder.easysftp.extensions.isIP
import kotlinx.android.synthetic.main.dialog_add_sftp_server.*

class StorageAddDialogFragment(private val dialogClickActionListener: DialogActionListener) : DialogFragment() {

    private lateinit var alertDialog: AlertDialog

    private val textWatcher by lazy {
        object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val serverIp = alertDialog.labeledEtServer.getText()
                val username = alertDialog.labeledEtUser.getText()
                val displayName = alertDialog.labeledEtName.getText()

                val isIpValid = alertDialog.labeledEtServer
                    .doLabelCheck("Please enter IP") {
                        !serverIp.isNullOrEmpty() && serverIp.isIP()
                    }
                val isUsernameValid = alertDialog.labeledEtUser
                    .doLabelCheck("Please enter user name") { !username.isNullOrBlank() }
                val isDisplayNameValid = alertDialog.labeledEtName
                    .doLabelCheck("Please enter display name") { !displayName.isNullOrBlank() }

                val shouldEnable = isIpValid && isUsernameValid && isDisplayNameValid
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = shouldEnable
            }

            override fun afterTextChanged(s: Editable?) = Unit

            private fun LabeledEditText.doLabelCheck(
                errMsg: String,
                checkErr: () -> Boolean
            ): Boolean {
                val result = checkErr()
                if (result) {
                    this.setError(null)
                } else {
                    this.setError(errMsg)
                }

                return result
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        alertDialog = AlertDialog.Builder(requireContext())
            .setView(inflater.inflate(R.layout.dialog_add_sftp_server, null))
            .setPositiveButton(R.string.add) { _, _ ->
                dialog?.let {
                    // Texts should not be null at this point since button is enabled only after
                    // the validations cheks were passed
                    val dataArr = arrayOf(
                        it.labeledEtServer.getText()!!,
                        it.labeledEtUser.getText()!!,
                        it.labeledEtName.getText()!!
                    )

                    dialogClickActionListener.onDialogPositiveClick(dataArr)
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                dialogClickActionListener.onDialogNegativeClick()
            }
            .create()

        return alertDialog
    }

    override fun onResume() {
        super.onResume()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        alertDialog.labeledEtServer.setTextWatcher(textWatcher)
        alertDialog.labeledEtUser.setTextWatcher(textWatcher)
        alertDialog.labeledEtName.setTextWatcher(textWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()

        alertDialog.labeledEtServer.removeTextWatcher(textWatcher)
        alertDialog.labeledEtUser.removeTextWatcher(textWatcher)
        alertDialog.labeledEtName.removeTextWatcher(textWatcher)
    }

    interface DialogActionListener {
        fun onDialogPositiveClick(result: Array<String>)
        fun onDialogNegativeClick() { }
    }
}