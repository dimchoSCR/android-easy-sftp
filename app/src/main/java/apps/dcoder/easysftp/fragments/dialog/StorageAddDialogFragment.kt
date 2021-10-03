package apps.dcoder.easysftp.fragments.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.custom.LabeledEditText
import apps.dcoder.easysftp.extensions.isIP
import apps.dcoder.easysftp.extensions.isFolderPath
import kotlinx.android.synthetic.main.dialog_add_sftp_server.*

class StorageAddDialogFragment(private val dialogClickActionListener: DialogActionListener<Array<String>>) : DialogFragment() {

    private lateinit var alertDialog: AlertDialog

    private val textWatcher by lazy {
        object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val serverFullPath = alertDialog.labeledEtServer.getText()
                val username = alertDialog.labeledEtUser.getText()
                val displayName = alertDialog.labeledEtName.getText()
                val folderPath = alertDialog.labeledEtFolderPath.getText()

                val isIpValid = alertDialog.labeledEtServer
                    .doLabelCheck("Please enter IP and folder path") {
                        !serverFullPath.isNullOrEmpty() && serverFullPath.isIP()
                    }
                val isUsernameValid = alertDialog.labeledEtUser
                    .doLabelCheck("Please enter user name") { !username.isNullOrBlank() }
                val isDisplayNameValid = alertDialog.labeledEtName
                    .doLabelCheck("Please enter display name") { !displayName.isNullOrBlank() }
                val isFolderPathValid = alertDialog.labeledEtFolderPath
                    .doLabelCheck("Please enter a folder path") { !folderPath.isNullOrEmpty() && folderPath.isFolderPath() }

                val shouldEnable = isIpValid && isUsernameValid && isDisplayNameValid && isFolderPathValid
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
                    // the validations checks were passed
                    val dataArr = arrayOf(
                        it.labeledEtServer.getText()!! + it.labeledEtFolderPath.getText()!!,
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
        alertDialog.labeledEtFolderPath.setTextWatcher(textWatcher)
    }

    override fun onDestroy() {
        super.onDestroy()

        alertDialog.labeledEtServer.removeTextWatcher(textWatcher)
        alertDialog.labeledEtUser.removeTextWatcher(textWatcher)
        alertDialog.labeledEtName.removeTextWatcher(textWatcher)
        alertDialog.labeledEtFolderPath.removeTextWatcher(textWatcher)
    }
}