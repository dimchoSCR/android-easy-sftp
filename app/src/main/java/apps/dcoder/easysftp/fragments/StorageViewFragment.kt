package apps.dcoder.easysftp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.services.StorageDiscoveryService
import org.koin.android.ext.android.inject

class StorageViewFragment : Fragment() {

    private val storageDiscoveryService: StorageDiscoveryService by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_storage_view, container, false)
    }

}
