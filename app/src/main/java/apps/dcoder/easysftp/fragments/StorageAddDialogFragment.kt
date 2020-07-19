package apps.dcoder.easysftp.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import apps.dcoder.easysftp.R
import java.lang.ClassCastException

class StorageAddDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = requireActivity().layoutInflater
        val dialogClickListener = requireParentFragment() as? StorageAddDialogListener
            ?: throw ClassCastException("Dialog host must implement the StorageAddDialogListener interface!")

        return AlertDialog.Builder(requireContext())
            .setView(inflater.inflate(R.layout.dialog_add_sftp_server, null))
            .setPositiveButton(R.string.add) { _, _ ->
                dialogClickListener.onDialogPositiveClick(this)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                dialogClickListener.onDialogNegativeClick(this)
            }
            .create()

    }

    interface StorageAddDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }
}