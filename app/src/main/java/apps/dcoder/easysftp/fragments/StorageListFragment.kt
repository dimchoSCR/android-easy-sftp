package apps.dcoder.easysftp.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.adapters.StorageEntryAdapter
import apps.dcoder.easysftp.model.status.Status
import apps.dcoder.easysftp.viewmodels.StorageListViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_storage_view.*
import kotlinx.android.synthetic.main.fragment_storage_view.view.*
import org.koin.android.viewmodel.ext.android.viewModel

class StorageListFragment : Fragment() {

    private val storageListViewModel: StorageListViewModel by viewModel()

    private val storageEntryAdapter = StorageEntryAdapter()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val progressRunnable = Runnable { pbLoading.visibility = View.VISIBLE }

    companion object {
        private const val DISPLAY_DELAY_LOADING_INDICATOR = 160L
    }

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
                Status.LOADING -> {
                    mainHandler.postDelayed(progressRunnable, DISPLAY_DELAY_LOADING_INDICATOR)
                }

                Status.SUCCESS -> {
                    hideProgressBar()
                    resource.data?.let { storageEntryAdapter.setStorageEntries(it) }
                }

                Status.ERROR -> {
                    hideProgressBar()
                    tvError.visibility = View.VISIBLE
                    tvError.text = getString(R.string.err_storage_not_loaded)
                }
            }
        })

        initializeListeners()
    }

    private fun hideProgressBar() {
        mainHandler.removeCallbacks(progressRunnable)
        pbLoading.visibility = View.GONE
    }

    private fun initializeListeners() {
        val sftpBottomSheet = BottomSheetBehavior.from(bsAddSftpServer)
        fabAddSftpServer.setOnClickListener {
            if (sftpBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED ||
                    sftpBottomSheet.state == BottomSheetBehavior.STATE_HIDDEN) {

                sftpBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        sftpBottomSheet.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) { }

            override fun onStateChanged(sheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> fabAddSftpServer.hide()
                    BottomSheetBehavior.STATE_HIDDEN, BottomSheetBehavior.STATE_COLLAPSED -> fabAddSftpServer.show()
                    else -> {  }
                }
            }

        })
    }
}
