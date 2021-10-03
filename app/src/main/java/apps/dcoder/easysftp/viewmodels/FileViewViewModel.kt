package apps.dcoder.easysftp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import apps.dcoder.easysftp.fragments.dialog.DialogActionListener
import apps.dcoder.easysftp.util.Event
import apps.dcoder.easysftp.util.LiveEvent
import apps.dcoder.easysftp.util.MutableLiveEvent
import java.util.Stack

enum class ProgressState {
    LOADING, IDLE
}
class FileViewViewModel(rootDirPath: String) : ViewModel(), DialogActionListener<String> {

    private var prevItemPos = 0
    private var prevScrollOffset = 0

    private val _progressState = MutableLiveEvent<ProgressState>(Event(ProgressState.LOADING))
    val progressState: LiveEvent<ProgressState> = _progressState

    private val _eventPasswordSet = MutableLiveEvent<String>()
    val eventPasswordSet: LiveEvent<String> = _eventPasswordSet

    val positionStack: Stack<Pair<Int, Int>> = Stack()
    var shouldUnbindFileService = false
    var lastListedDir: String = rootDirPath
    var serviceHasBeenKilled: Boolean = false

    fun updateProgressState(progressState: ProgressState) {
        _progressState.value = Event(progressState)
    }

    fun popSavedScrollPositions() {
        val positionPair = positionStack.pop()
        val (prevItemPos, prevScrollOffset) = positionPair

        this.prevItemPos = prevItemPos
        this.prevScrollOffset = prevScrollOffset
    }

    fun getSavedScrollPositions(): Pair<Int, Int> {
        return Pair(prevItemPos, prevScrollOffset)
    }

    fun resetRvPositions() {
        prevItemPos = 0
        prevScrollOffset = 0
    }

    override fun onDialogPositiveClick(result: String) {
        Log.d("DMK", "Pass entered: $result")
        _eventPasswordSet.value = Event(result)
    }
}