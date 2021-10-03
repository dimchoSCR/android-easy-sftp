package apps.dcoder.easysftp.fragments.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import apps.dcoder.easysftp.R
import kotlinx.android.synthetic.main.dialog_pass_prompt.*
import kotlinx.android.synthetic.main.dialog_pass_prompt.view.*
import java.util.regex.Pattern

class PasswordPromptDialog(private val serverAddress: String, private val dialogClickActionListener: DialogActionListener<String>) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_pass_prompt, null)
        view.labeledEtPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton(R.string.add) { _, _ ->
                dialog?.let {
                    it.labeledEtPassword.getText()?.let { pass ->
                        dialogClickActionListener.onDialogPositiveClick(pass)
                    }
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                dialogClickActionListener.onDialogNegativeClick()
            }
            .create()

        val serverIp = serverAddress.split(Pattern.compile("/"), 2)[0]
        view.tvTitle.text = getString(R.string.password_for, serverIp)

        return dialog
    }
}