package apps.dcoder.easysftp.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.adapters.StorageEntryAdapter
import apps.dcoder.easysftp.model.LocalStorageInfo
import apps.dcoder.easysftp.model.RemoteStorageInfo
import apps.dcoder.easysftp.model.StorageInfo
import apps.dcoder.easysftp.model.androidModel.AdaptableLocalStorageInfo
import apps.dcoder.easysftp.model.androidModel.AdaptableRemoteStorageInfo
import apps.dcoder.easysftp.model.androidModel.AdaptableStorageInfo
import apps.dcoder.easysftp.model.status.Status
import apps.dcoder.easysftp.viewmodels.StorageListViewModel
import kotlinx.android.synthetic.main.fragment_storage_view.*
import kotlinx.android.synthetic.main.fragment_storage_view.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs

class StorageListFragment : Fragment() {

    private val storageListViewModel: StorageListViewModel by viewModel()
    private val storageAddDialogFragment: StorageAddDialogFragment by inject()

    private val storageEntryAdapter = StorageEntryAdapter()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val progressRunnable = Runnable { pbLoading.visibility = View.VISIBLE }

    companion object {
        private const val DISPLAY_DELAY_LOADING_INDICATOR = 160L
        private const val TAG_ADD_SFTP_STORAGE_DIALOG = "AddSFTPStorage"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_storage_view, container, false)
        view.lvStorageVolumes.adapter = storageEntryAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storageListViewModel.storageOptionsLiveData.observe(this.viewLifecycleOwner, { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    mainHandler.postDelayed(progressRunnable, DISPLAY_DELAY_LOADING_INDICATOR)
                }

                Status.SUCCESS -> {
                    hideProgressBar()
                    if (resource.data != null) {
                        val adaptableStorageEntries = convertToAdaptableStorageEntries(resource.data)
                        if (storageEntryAdapter.itemCount == 0) {
                            storageEntryAdapter.setStorageEntries(adaptableStorageEntries)
                        } else {
                            // Inserting items
                            val itemSizeDelta = adaptableStorageEntries.size - storageEntryAdapter.itemCount
                            if (itemSizeDelta > 0) {
                                storageEntryAdapter.insertStorageEntries(
                                    adaptableStorageEntries,
                                    storageEntryAdapter.itemCount,
                                    itemSizeDelta
                                )
                            } else {
                                resource.payload?.let {
                                    storageEntryAdapter.deleteStorageEntry(it)
                                }
                            }
                        }
                    } else {
                       Log.e(this::class.java.simpleName, "Storage list should not be null")
                    }
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

    private fun convertToAdaptableStorageEntries(storageEntries: List<StorageInfo>): List<AdaptableStorageInfo> {
        val adaptableStorageInfos = mutableListOf<AdaptableStorageInfo>()

        for (storageEntry in storageEntries) {
            when (storageEntry) {
                is LocalStorageInfo -> {
                    val (nameResource, storageIcon) = if (storageEntry.isRemovable) {
                        getString(R.string.removable_storage) to R.drawable.ic_sd_storage_black_24dp
                    } else {
                        getString(R.string.internal_storage) to R.drawable.ic_storage_black_24dp
                    }

                    val adaptableStorageInfo = AdaptableLocalStorageInfo.fromLocalStorageInfo(storageEntry, nameResource, storageIcon)
                    adaptableStorageInfos.add(adaptableStorageInfo)
                }

                is RemoteStorageInfo -> {
                    val adaptableStorageInfo = AdaptableRemoteStorageInfo.fromRemoteStorageInfo(storageEntry, R.drawable.ic_storage_black_24dp)
                    adaptableStorageInfos.add(adaptableStorageInfo)
                }
            }
        }

        return adaptableStorageInfos
    }

    private fun initializeListeners() {
        fabAddSftpServer.setOnClickListener {
            storageAddDialogFragment.show(childFragmentManager, TAG_ADD_SFTP_STORAGE_DIALOG)
        }

        storageEntryAdapter.onItemClickListener = { position ->

        }

        storageEntryAdapter.onItemLongClickListener = { position, view ->
            val adaptableItem = storageEntryAdapter.getItem(position)
            if (adaptableItem is AdaptableRemoteStorageInfo) {
                val popupMenu = PopupMenu(requireContext(), view)
                popupMenu.inflate(R.menu.storage_popup_menu)
                popupMenu.show()

                popupMenu.setOnMenuItemClickListener {
                    if (it.itemId == R.id.menu_delete) {
                        storageListViewModel.removeRemoteStorageItem(adaptableItem.ip)
                    }

                    true
                }
            }
        }
    }

}
