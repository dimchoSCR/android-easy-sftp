package apps.dcoder.easysftp.filemanager

import apps.dcoder.easysftp.extensions.isFolderPath

object ClipBoardManager {
    data class ClipBoardEntry(val isLocalFile: Boolean, var filePath: String, val fileNameWithExt: String)

    var clipBoardEntry: ClipBoardEntry? = null
        private set

    val isClipboardEmpty: Boolean
        get() = clipBoardEntry == null

    fun saveFilePath(isLocal: Boolean, filePath: String, fileNameWithExt: String) {
        clipBoardEntry = ClipBoardEntry(isLocal, filePath, fileNameWithExt)
    }

    fun clear() {
        clipBoardEntry = null
    }
}