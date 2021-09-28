package apps.dcoder.easysftp.filemanager

import apps.dcoder.easysftp.model.FileInfo
import java.util.ArrayList
import java.util.LinkedHashMap

//
//import android.os.Handler
//import android.os.Looper
//import android.os.Message
//import com.jcraft.jsch.UserInfo
//import java.io.File
//import java.util.*
//import kotlin.collections.ArrayList
//import kotlin.collections.HashMap
//import kotlin.collections.HashSet
//
//const val ACTION_LIST_DIRECTORY = 5
//const val ACTION_QUIT_HANDLER_THREAD = -1
//
//class RemoteFileManager(private val userInfo: UserInfo): FileManager {
//
//    private val mainHandler: MainHandler = MainHandler(Looper.getMainLooper())
//    private var fileHandlerThread: RemoteFileHandlerThread
//            = RemoteFileHandlerThread(mainHandler, userInfo)
//    private val listedFiles: java.util.ArrayList<FileInfo> = java.util.ArrayList(30)
//
//    private lateinit var onFileManagerPrepared: (fm: FileManager) -> Unit
//    private lateinit var onFileManagerResultListener: OnFileManagerResultListener
//
//    private var quitMessageSent: Boolean = false
//
//    private val idsOfTasksToBeCancelled: HashSet<String> = HashSet()
//    private val runningTaskIdQueue: LinkedList<String> = LinkedList()
//    private val taskTypeToActionMap: HashMap<String, Int> = hashMapOf(
//        TaskType.LIST.value to ACTION_LIST_DIRECTORY
//    )
//
//    private fun clearCancelledTasks(currentlyFinishedTaskIdIndex: Int) {
//        for(i in 0 until currentlyFinishedTaskIdIndex) {
//            idsOfTasksToBeCancelled.remove(runningTaskIdQueue.poll())
//        }
//    }
//
//    private inner class MainHandler(looper: Looper): Handler(looper) {
//        override fun handleMessage(msg: Message) {
//            // Stop handling of pending task, because thread quit
//            if(quitMessageSent) {
//                return
//            }
//
//            // May be null because the empty quit task does not require an id
//            val currentlyFinishedTaskID: String? = fileHandlerThread.finishingTaskIdQueue.poll()
//            // Clear accumulated cancelled tasks in the runningTaskIdQueue
//            // and in idsOfTasksToBeCancelled set
//            // Tasks are accumulated in both structures when handler.removeMessages(what, obj)
//            // is called. Since the removeMessages method does not guarantee message removal
//            // we don't know if we should clear the task we attempted to cancel
//            clearCancelledTasks(runningTaskIdQueue.indexOf(currentlyFinishedTaskID))
//            // Remove the currently finished task from the queue
//            runningTaskIdQueue.remove(currentlyFinishedTaskID)
//
//            when(msg.what) {
//                // HandlerThread is up and running so call onFileManagerPrepared()
//                RemoteFileHandlerThread.RESULT_CHANNEL_CONNECTED -> {
//                    onFileManagerPrepared(this@RemoteFileManager)
//                }
//
//                RemoteFileHandlerThread.RESULT_LISTED_DIRECTORY -> {
//                    val files: java.util.ArrayList<FileInfo> = msg.obj as java.util.ArrayList<FileInfo>
//                    shouldReInitiateDirListing = false
//                    val pathToCurrentlyListedDir = currentlyFinishedTaskID!!
//                        .split(Regex.fromLiteral(":"))[1]
//
////                    fileHandlerThread.taskStatusMap[currentlyFinishedTaskID] = RemoteFileHandlerThread.TaskStatus.FINISHED
//
//                    // TODO if cache is running low clear previous entry
//                    // Insert the listed directory in the cache
//                    if(!files.isEmpty()) {
//                        filesCache[pathToCurrentlyListedDir] = ArrayList(files)
//                    }
//
//                    // Cancel the result of the task if it was cancelled by the user
//                    if(idsOfTasksToBeCancelled.remove(currentlyFinishedTaskID)) {
//                        return
//                    }
//
//                    listedFiles.clear()
//                    listedFiles.addAll(files)
//
//                    onFileManagerResultListener.onFilesListed()
//                }
//
//                else -> super.handleMessage(msg)
//            }
//        }
//    }
//
//    override val rootDirectoryPath: String = "/export/content/downloads"
//
//    override val filesCache: LinkedHashMap<String, List<FileInfo>> = linkedMapOf()
//
//    override var shouldReInitiateDirListing: Boolean = true
//
//    override fun prepare(onPrepared: (fm: FileManager) -> Unit) {
//        fileHandlerThread = RemoteFileHandlerThread(mainHandler, userInfo)
//        fileHandlerThread.start()
//
//        onFileManagerPrepared = onPrepared
//    }
//
//    override fun setOnFileManagerResultListener(listener: OnFileManagerResultListener) {
//        onFileManagerResultListener = listener
//    }
//
//    override fun getCurrentlyListedFiles(): java.util.ArrayList<FileInfo> {
//        return listedFiles
//    }
//
//    override fun listDirectory(dirPath: String) {
//        val handler = fileHandlerThread.handler
////        pathToCurrentlyListedDir = dirPath
//
//        val taskId = TaskType.LIST.constructTaskId(dirPath)
//        runningTaskIdQueue.add(taskId)
////        mp[taskId] = dirPath
////        fileHandlerThread.taskStatusMap[taskId] = RemoteFileHandlerThread.TaskStatus.PENDING
//
//        handler.sendMessage (
//            handler.obtainMessage(ACTION_LIST_DIRECTORY, taskId)
//        )
//    }
//
//    override fun getParentDirectoryPath(dir: String): String {
//        return dir.substring(0, dir.lastIndexOf(File.separatorChar))
//    }
//
////    override fun clearPendingTaskResultsFromActivity(resultID: Int) {
////        mainHandler.removeMessages(resultID)
////    }
////
////    override fun findIdOfTaskToCancel(): String? {
////        for (currentRunningTaskID in runningTaskIdQueue) {
////            if (idsOfTasksToBeCancelled.contains(currentRunningTaskID)) {
////                continue
////            }
////
////            return currentRunningTaskID
////        }
////
////        return null
////    }
////
////    override fun cancelTaskWithID(taskID: String) {
////        val splitStringList = taskID.split(Regex.fromLiteral(":"))
////        val messageWhat: Int = taskTypeToActionMap[splitStringList.first()]
////            ?: throw Exception("No such key in taskTypeToActionMap")
////
////        // Try to prevent queued task from running
////        // This operation may or may not succeed
////        // So add the task's id to the shouldBeCancelledIfReceived set
////        fileHandlerThread.handler.removeMessages(messageWhat, taskID)
////        idsOfTasksToBeCancelled.add(taskID)
////    }
//
//    override fun exit() {
//        super.exit()
//
//        quitMessageSent = true
//        fileHandlerThread.handler.sendEmptyMessage(ACTION_QUIT_HANDLER_THREAD)
//    }
//}
class RemoteFileManager : FileManager {
    override val rootDirectoryPath: String = ""
    override val filesCache: LinkedHashMap<String, List<FileInfo>> = linkedMapOf()
    override var currentDir: String = ""

    override fun prepare(onPrepared: () -> Unit) = Unit

    override fun setOnFileManagerResultListener(listener: OnFileManagerResultListener) = Unit

    override fun listDirectory(dirPath: String, forceRefresh: Boolean): List<FileInfo> = listOf()
    override fun listParent(): List<FileInfo> = emptyList()

    override fun getParentDirectoryPath(dir: String): String = ""

    override fun getCurrentlyListedFiles(): ArrayList<FileInfo> = arrayListOf<FileInfo>()
    override fun putInCache(dirPath: String, files: List<FileInfo>) = Unit

}