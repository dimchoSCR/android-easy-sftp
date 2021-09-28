package apps.dcoder.easysftp.fragments

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import apps.dcoder.easysftp.R
import apps.dcoder.easysftp.adapters.FilesAdapter
import apps.dcoder.easysftp.filemanager.ListItemClickListener
import apps.dcoder.easysftp.fragments.StorageListFragment.Companion.ARG_ROOT_DIR_PATH
import apps.dcoder.easysftp.model.FileInfo
import apps.dcoder.easysftp.services.android.FileManagerService
import apps.dcoder.easysftp.viewmodels.FileViewViewModel
import apps.dcoder.easysftp.viewmodels.ProgressState
import kotlinx.android.synthetic.main.fragment_file_view.*
import kotlinx.android.synthetic.main.fragment_file_view.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import android.content.Intent
import apps.dcoder.easysftp.extensions.isServiceRunning
import apps.dcoder.easysftp.util.status.Status
import apps.dcoder.easysftp.services.android.FileManagerOperationResult

class FileViewFragment: Fragment(), ListItemClickListener {

    private lateinit var animation: LayoutAnimationController

    private val viewModel: FileViewViewModel by viewModel {
        parametersOf(requireArguments().getString(ARG_ROOT_DIR_PATH))
    }

    private val filesAdapter = FilesAdapter(this)

    private lateinit var fileManagerService: FileManagerService

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            fileManagerService = (service as FileManagerService.FileManagerBinder)
                .getService(requireArguments().getString(ARG_ROOT_DIR_PATH, ""))

            addObservers(requireView())
            initFileManager()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e(this::class.java.simpleName, "FileManagerService crashed!")
        }
    }

    private fun initFileManager() {
        // Initializes the fm and lists all files when fm is ready and
        fileManagerService.prepare {
            if (viewModel.serviceHasBeenKilled) {
                fileManagerService.restoreFileManagerStateAfterServiceRestart(
                    viewModel.lastListedDir,
                    filesAdapter.getFileList()
                )
                viewModel.serviceHasBeenKilled = false
            } else {
                viewModel.updateProgressState(ProgressState.LOADING)
                fileManagerService.listCurrentDirectory()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bindFileManagerService()
    }

    private fun bindFileManagerService() {
        val bindSuccessful = requireActivity().bindService(
            Intent(requireActivity(), FileManagerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )

        if (bindSuccessful) {
            Log.i(this::class.java.simpleName, "Binding to service successful!")
            viewModel.shouldUnbindFileService = true
        } else {
            Log.e(this::class.java.simpleName, "Error: The requested service doesn't " +
                    "exist, or this client isn't allowed access to it.")
        }
    }

    private fun unbindFileManagerService() {
        if (viewModel.shouldUnbindFileService) {
            requireActivity().unbindService(serviceConnection)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.wtf("Test", "Fragment OnCreateView")
        val filesView = inflater.inflate(
            R.layout.fragment_file_view,
            container, false
        )

        if (savedInstanceState != null) {
            filesView.ltSwipeToRefresh.isRefreshing =
                savedInstanceState.getBoolean("PullToRefresh")
        }

        // Set up swipe to refresh
        val ltRefresh = filesView.ltSwipeToRefresh
        ltRefresh.setColorSchemeResources(R.color.colorAccent)
        ltRefresh.setOnRefreshListener {
            viewModel.resetRvPositions()
            fileManagerService.listCurrentDirectory(true)
        }
        ltRefresh.isEnabled = false

        return filesView
    }


    private fun setUpRecyclerView(rootView: View) {
        val recycler = rootView.recyclerView
        // Improves performance
        recycler.setHasFixedSize(true)

        // Sets the layout manager
        val layoutManager = LinearLayoutManager(context)
        recycler.layoutManager = layoutManager

        // Sets an item decoration
        val dividerItemDecoration = DividerItemDecoration(
            context,
            layoutManager.orientation
        )
        recycler.addItemDecoration(dividerItemDecoration)

        recycler.adapter = filesAdapter

        // Sets up the recycler view's layout animation
        animation = AnimationUtils.loadLayoutAnimation(context, R.anim.rv_animation_fall)
        recycler.layoutAnimation = animation
    }

    private fun addObservers(filesView: View) {
        viewModel.progressState.consume(this.viewLifecycleOwner) {
            // If the cache is not empty and there are no files in the file manager
            // then the directory is empty
            if (fileManagerService.getCurrentlyListedFiles().isEmpty() && it == ProgressState.IDLE) {
                filesView.tvEmpty.visibility = View.VISIBLE
            }

            // If cache is still empty and cache has not been cleared then the files are not loaded yet
            // So show progress indicator
            if (it == ProgressState.LOADING) {
                filesView.progressBar.visibility = View.VISIBLE
            } else {
                filesView.progressBar.visibility = View.GONE
            }
        }

        fileManagerService.fileManagerOperationLiveData.consume(this.viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    resource.data?.let { processFileManagerResult(it) }
                }
                Status.ERROR -> {}
            }
        }
    }

    private fun processFileManagerResult(opResult: FileManagerOperationResult) {
        when (opResult) {
            is FileManagerOperationResult.ListOperationResult -> {
                onFilesListed(opResult.files)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this.viewLifecycleOwner) {
            onBackPressed()
        }

        setUpRecyclerView(requireView())
    }

    private fun onFilesListed(fileList: List<FileInfo>) {
        ltSwipeToRefresh.isEnabled = true
        ltSwipeToRefresh.isRefreshing = false

        viewModel.updateProgressState(ProgressState.IDLE)

        recyclerView.startLayoutAnimation()
        filesAdapter.updateFileList(fileList)

        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        val (prevItemPos, prevScrollOffset) = viewModel.getSavedScrollPositions()
        linearLayoutManager.scrollToPositionWithOffset(prevItemPos, prevScrollOffset)

        if (fileList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }
    }

    override fun onListItemClick(clickedItemIndex: Int) {
        val clickedFileInfo: FileInfo = fileManagerService.getCurrentlyListedFiles()[clickedItemIndex]

        if (clickedFileInfo.isDirectory) {
            viewModel.updateProgressState(ProgressState.LOADING)

            // Acquire current list item's position and offset
            // Store the data in a stack
            // Then list the new directory
            val childAtTopOfList: View = recyclerView.getChildAt(0)
            val prevItemPos = recyclerView.getChildAdapterPosition(childAtTopOfList)
            val prevScrollOffset = childAtTopOfList.top

            // Save the scroll position for this directory
            viewModel.positionStack.add(Pair(prevItemPos, prevScrollOffset))

            // Reset position variables
            viewModel.resetRvPositions()

            fileManagerService.listDirectory(clickedFileInfo.absolutePath)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("PullToRefresh", ltSwipeToRefresh.isRefreshing)
    }

    private fun onBackPressed() {
        if (!fileManagerService.isOnRootDir()) {
            recyclerView.stopScroll()

            // Stop the result of a currently running task
//            val idOFTaskToCancel = fileManager.findIdOfTaskToCancel()
//            if (idOFTaskToCancel != null) {
//                fileManager.cancelTaskWithID(idOFTaskToCancel)
//                fileManager.clearPendingTaskResultsFromActivity(
//                    RemoteFileHandlerThread.RESULT_LISTED_DIRECTORY
//                )
//            }

            // Restore the lists previous state
            viewModel.popSavedScrollPositions()

            // Gets the parent directory of the currentDirectory
            // Then updates the currentDirPath reference
            fileManagerService.listParent()

        } else {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()

        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (!activityManager.isServiceRunning(FileManagerService::class.java)) {
            unbindFileManagerService()
            requireActivity().startService(Intent(requireContext(), FileManagerService::class.java))
            viewModel.serviceHasBeenKilled = true
            bindFileManagerService()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.lastListedDir = fileManagerService.getCurrentDir()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this.isRemoving) {
            Log.wtf("Test", "Fragment removing. Thread destroying")
            fileManagerService.exit()
        }

        unbindFileManagerService()

        Log.wtf("Test", "On fragment destroy")
    }
}