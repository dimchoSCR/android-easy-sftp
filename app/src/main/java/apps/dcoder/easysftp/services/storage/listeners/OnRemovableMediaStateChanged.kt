package apps.dcoder.easysftp.services.storage.listeners

import apps.dcoder.easysftp.services.storage.RemovableMediaState

interface OnRemovableMediaStateChanged {
    fun onMediaStateChanged(pathToMedia: String, mediaState: RemovableMediaState)
}