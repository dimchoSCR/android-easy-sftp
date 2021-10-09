package apps.dcoder.easysftp.filemanager

import android.view.View

interface ListItemClickListener {
    fun onListItemClick(clickedItemIndex: Int)
    fun onLongClick(clickedItemIndex: Int, view: View)
}