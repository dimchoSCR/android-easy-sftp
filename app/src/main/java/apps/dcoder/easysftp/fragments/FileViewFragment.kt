package apps.dcoder.easysftp.fragments

import android.os.Bundle
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
import apps.dcoder.easysftp.filemanager.OnFileManagerResultListener
import apps.dcoder.easysftp.fragments.StorageListFragment.Companion.ARG_ROOT_DIR_PATH
import apps.dcoder.easysftp.model.FileInfo
import apps.dcoder.easysftp.viewmodels.FileViewViewModel
import apps.dcoder.easysftp.viewmodels.ProgressState
import kotlinx.android.synthetic.main.fragment_file_view.*
import kotlinx.android.synthetic.main.fragment_file_view.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class FileViewFragment: Fragment(), ListItemClickListener,
    OnFileManagerResultListener {

    private lateinit var animation: LayoutAnimationController
    private var prevScrollOffset: Int = 0
    private var prevItemPos: Int = 0

    private val viewModel: FileViewViewModel by viewModel {
        parametersOf(requireArguments().getString(ARG_ROOT_DIR_PATH))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

            prevItemPos = savedInstanceState.getInt("PrevItemPos")
            prevScrollOffset = savedInstanceState.getInt("PrevScrollOffset")
        }

        setUpRecyclerView(filesView)
        addObservers(filesView)

        // Set up swipe to refresh
        val ltRefresh = filesView.ltSwipeToRefresh
        ltRefresh.setColorSchemeResources(R.color.colorAccent)
        ltRefresh.setOnRefreshListener {
            prevItemPos = 0
            prevScrollOffset = 0

            viewModel.fileManager.listDirectory(viewModel.currentDirPath)
        }

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

        recycler.adapter = FilesAdapter(viewModel.fileManager.getCurrentlyListedFiles(), this)

        // Sets up the recycler view's layout animation
        animation = AnimationUtils.loadLayoutAnimation(context, R.anim.rv_animation_fall)
        recycler.layoutAnimation = animation
    }

    private fun addObservers(filesView: View) {
        viewModel.progressState.observe(this.viewLifecycleOwner) {
            // If the cache is not empty and there are no files in the file manager
            // then the directory is empty
            if (viewModel.getCurrentlyListedFiles().isEmpty() && it == ProgressState.IDLE) {
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this.viewLifecycleOwner) {
            onBackPressed()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.fileManager.setOnFileManagerResultListener(this)

        // Initializes the fm and lists all files when fm is ready and
        if (savedInstanceState == null) {
            ltSwipeToRefresh.isEnabled = false
            viewModel.updateProgressState(ProgressState.LOADING)
            viewModel.fileManager.prepare { fm -> fm.listDirectory(viewModel.currentDirPath); }
        }
    }

    override fun onFilesListed() {
        ltSwipeToRefresh.isEnabled = true
        ltSwipeToRefresh.isRefreshing = false
//        recyclerView.visibility = View.VISIBLE
        viewModel.updateProgressState(ProgressState.IDLE)

        recyclerView.startLayoutAnimation()
        recyclerView.adapter?.notifyDataSetChanged()

        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        linearLayoutManager.scrollToPositionWithOffset(prevItemPos, prevScrollOffset)

        if (viewModel.fileManager.getCurrentlyListedFiles().isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }
    }

    override fun onListItemClick(clickedItemIndex: Int) {
        val clickedFileInfo: FileInfo = viewModel.fileManager.getCurrentlyListedFiles()[clickedItemIndex]

        if (clickedFileInfo.isDirectory) {
            viewModel.updateProgressState(ProgressState.LOADING)

            // Acquire current list item's position and offset
            // Store the data in a stack
            // Then list the new directory
            val childAtTopOfList: View = recyclerView.getChildAt(0)
            prevItemPos = recyclerView.getChildAdapterPosition(childAtTopOfList)
            prevScrollOffset = childAtTopOfList.top
            viewModel.stateStack.add(Pair(prevItemPos, prevScrollOffset))

            // Reset position variables
            prevItemPos = 0
            prevScrollOffset = 0

            val prevDirPath = viewModel.currentDirPath
            viewModel.currentDirPath = clickedFileInfo.absolutePath

            // TODO add cache limit
            // If there are cache entries load and the requested item is in the cache display it
            if (viewModel.fileManager.useCachedFolder(viewModel.currentDirPath)) {
                Log.wtf("Test", "From cache")
                onFilesListed()
            } else {
                Log.wtf("Test", "No cache")

                // Clear accumulated cache when the user navigates
                // to a directory for the first time from the storage root
                if (viewModel.fileManager.filesCache.size > 1 && prevDirPath == viewModel.rootDirPath) {
                    // Clears only the children of the root Directory
                    viewModel.fileManager.clearChildrenFromCache(viewModel.rootDirPath)
                    Log.wtf("Test", "Clearing cache")
                }

                // Do not invoke if listing the root Directory
                viewModel.fileManager.listDirectory(clickedFileInfo.absolutePath)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        Log.wtf("Test", "saving state")
        outState.putInt("PrevItemPos", prevItemPos)
        outState.putInt("PrevScrollOffset", prevScrollOffset)
        outState.putBoolean("PullToRefresh", ltSwipeToRefresh.isRefreshing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


    private fun onBackPressed() {
        if (viewModel.currentDirPath != viewModel.rootDirPath) {
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
            val (prevItemPos, prevScrollOffset) = viewModel.stateStack.pop()
            this.prevItemPos = prevItemPos
            this.prevScrollOffset = prevScrollOffset

            // Gets the parent directory of the currentDirectory
            // Then updates the currentDirPath reference
            viewModel.currentDirPath = viewModel.fileManager.getParentDirectoryPath(viewModel.currentDirPath)

            // Use a cached entry or list the directory if cache is empty
            if (viewModel.fileManager.useCachedFolder(viewModel.currentDirPath)) {
                Log.wtf("Test", "From cache")
                onFilesListed()
            } else {
                Log.wtf("Test", "No cache")
                viewModel.updateProgressState(ProgressState.LOADING)
                viewModel.fileManager.listDirectory(viewModel.currentDirPath)
            }

        } else {
            findNavController().popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this.isRemoving) {
            Log.wtf("Test", "Fragment removing. Thread destroying")
            viewModel.fileManager.exit()
        }

        Log.wtf("Test", "On fragment destroy")
    }
}