package apps.dcoder.easysftp.fragments.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import apps.dcoder.easysftp.R
import kotlinx.android.synthetic.main.dialog_pass_prompt.*
import kotlinx.android.synthetic.main.dialog_pass_prompt.view.*

class EditTextDialog(
    private val title: String,
    private val positiveButtonText: String,
    private val negativeButtonText: String,
) : DialogFragment() {
    var dialogClickActionListener: DialogActionListener<String>? = null
    var inputFieldText = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_pass_prompt, null)
        view.tvTitle.text = title
        view.labeledEtPassword.setLabelText("")
        view.labeledEtPassword.setHint("")
        view.labeledEtPassword.hideLabel()
        view.labeledEtPassword.setText(inputFieldText)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton(positiveButtonText) { _, _ ->
                dialog?.let {
                    it.labeledEtPassword.getText()?.let { pass ->
                        dialogClickActionListener?.onDialogPositiveClick(pass)
                    }
                }
            }
            .setNegativeButton(negativeButtonText) { _, _ ->
                dialogClickActionListener?.onDialogNegativeClick()
            }
            .create()

        return dialog
    }
}