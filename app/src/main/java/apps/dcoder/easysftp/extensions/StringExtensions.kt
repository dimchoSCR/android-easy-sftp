package apps.dcoder.easysftp.extensions

private val ipCheckRegex by lazy {
    Regex("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$")
}

private val folderPathCheckRegex by lazy {
    Regex("^/\$|(/[a-zA-Z_0-9-]+)+\$")
}

fun String.isIP(): Boolean = ipCheckRegex.matches(this)

fun String.isFolderPath(): Boolean = folderPathCheckRegex.matches(this)