package apps.dcoder.easysftp.filemanager

import apps.dcoder.easysftp.extensions.isFolderPath

object ClipBoardManager {
    data class ClipBoardEntry(val isLocalFile: Boolean, var filePath: String) {
        val fileNameWithExt
            get() = filePath.substring(filePath.lastIndexOf('/') + 1)
    }

    var clipBoardEntry: ClipBoardEntry? = null
        private set

    val isClipboardEmpty: Boolean
        get() = clipBoardEntry == null

    fun saveFilePath(isLocal: Boolean, filePath: String) {
        clipBoardEntry = ClipBoardEntry(isLocal, filePath)
    }

    fun clear() {
        clipBoardEntry = null
    }
}