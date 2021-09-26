package apps.dcoder.easysftp.model

import apps.dcoder.easysftp.R
import com.jcraft.jsch.ChannelSftp
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by dimcho on 10.03.18.
 */

fun getFileInfoFromFile(file: File): FileInfo {

    val name: String = file.name
    val lastEdit: String = SimpleDateFormat("MMM dd, yyyy, HH:mm", Locale.getDefault())
            .format(file.lastModified())
    val absolutePath: String = file.absolutePath

    val imgResource: Int
    val fileType: FileInfo.FileTypes
    val isFile: Boolean
    val isDirectory: Boolean

    if (file.isDirectory) {
        imgResource = R.drawable.ic_folder_24dp
        fileType = FileInfo.FileTypes.DIRECTORY
        isFile = false
        isDirectory = true

    } else {
        imgResource = R.drawable.ic_file_24dp
        fileType = FileInfo.FileTypes.FILE
        isDirectory = false
        isFile = true
    }

    return FileInfo(imgResource, fileType, name,
            lastEdit, isFile, isDirectory, absolutePath)
}

fun getFileInfoFromSftp(lsEntry: ChannelSftp.LsEntry, dirPath: String): FileInfo {
    val sftpATTRS = lsEntry.attrs

    val name: String = lsEntry.filename
    val lastEdit: String = SimpleDateFormat("MMM dd, yyyy, HH:mm", Locale.getDefault())
            .format(sftpATTRS.aTime)
    val absolutePath: String = "$dirPath${File.separatorChar}$name"

    val imgResource: Int
    val fileType: FileInfo.FileTypes
    val isFile: Boolean
    val isDirectory: Boolean

    if(sftpATTRS.isDir) {
        imgResource = R.drawable.ic_folder_24dp
        fileType = FileInfo.FileTypes.DIRECTORY
        isFile = false
        isDirectory = true

    } else {
        imgResource = R.drawable.ic_file_24dp
        fileType = FileInfo.FileTypes.FILE
        isDirectory = false
        isFile = true
    }

    return FileInfo(imgResource, fileType, name,
            lastEdit, isFile, isDirectory, absolutePath)
}

data class FileInfo(
        val imgResource: Int, val fileType: FileTypes, val name: String,
        val lastEdit: String, val isFile: Boolean,
        val isDirectory: Boolean, val absolutePath: String): Serializable {

    enum class FileTypes(val value: String) {
        FILE("File"), DIRECTORY("Directory")
    }
}

