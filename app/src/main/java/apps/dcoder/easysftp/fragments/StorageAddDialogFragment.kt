package apps.dcoder.easysftp.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import apps.dcoder.easysftp.R
import kotlinx.android.synthetic.main.dialog_add_sftp_server.*

class StorageAddDialogFragment(private val dialogClickActionListener: DialogActionListener) : DialogFragment() {

    companion object {
        const val KEY_STORAGE_DATA = "STORAGE_DATA"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater

        return AlertDialog.Builder(requireContext())
            .setView(inflater.inflate(R.layout.dialog_add_sftp_server, null))
            .setPositiveButton(R.string.add) { _, _ ->
                val bundle = Bundle()
                val dataArr = arrayOf(dialog!!.labeledEtServer.getText(), dialog!!.labeledEtUser.getText(), dialog!!.labeledEtName.getText())

                bundle.putStringArray(KEY_STORAGE_DATA, dataArr)
                dialogClickActionListener.onDialogPositiveClick(bundle)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                dialogClickActionListener.onDialogNegativeClick()
            }
            .create()
    }

    interface DialogActionListener {
        fun onDialogPositiveClick(result : Bundle)
        fun onDialogNegativeClick() { }
    }

}