package apps.dcoder.easysftp.viewmodels

import androidx.lifecycle.ViewModel
import apps.dcoder.easysftp.util.Event
import apps.dcoder.easysftp.util.LiveEvent
import apps.dcoder.easysftp.util.MutableLiveEvent
import java.util.Stack

enum class ProgressState {
    LOADING, IDLE
}
class FileViewViewModel(rootDirPath: String) : ViewModel() {

    private var prevItemPos = 0
    private var prevScrollOffset = 0

    private val _progressState = MutableLiveEvent<ProgressState>()
    var progressState: LiveEvent<ProgressState> = _progressState

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
}