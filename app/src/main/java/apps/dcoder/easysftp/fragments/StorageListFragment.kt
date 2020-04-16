package apps.dcoder.easysftp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.adapters.StorageEntryAdapter
import apps.dcoder.easysftp.model.status.Status
import apps.dcoder.easysftp.viewmodels.StorageListViewModel
import kotlinx.android.synthetic.main.fragment_storage_view.view.*
import org.koin.android.viewmodel.ext.android.viewModel

class StorageListFragment : Fragment() {

    private val storageListViewModel: StorageListViewModel by viewModel()

    private val storageEntryAdapter = StorageEntryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_storage_view, container, false)
        view.lvStorageVolumes.adapter = storageEntryAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storageListViewModel.storageOptionsLiveData.observe(this.viewLifecycleOwner, Observer { resource ->
            when (resource.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> resource.data?.let { storageEntryAdapter.setStorageEntries(it) }
                Status.ERROR -> {}
            }
        })
    }

}
