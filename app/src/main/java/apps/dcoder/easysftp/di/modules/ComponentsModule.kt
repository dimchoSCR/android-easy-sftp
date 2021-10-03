package apps.dcoder.easysftp.di.modules

import apps.dcoder.easysftp.fragments.dialog.PasswordPromptDialog
import apps.dcoder.easysftp.fragments.dialog.StorageAddDialogFragment
import apps.dcoder.easysftp.viewmodels.FileViewViewModel
import apps.dcoder.easysftp.viewmodels.StorageListViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val componentsModule = module {
    factory { StorageAddDialogFragment(get<StorageListViewModel>()) }
    factory { (serverAddress: String) ->
        PasswordPromptDialog(
            serverAddress,
            get<FileViewViewModel> { parametersOf(serverAddress) }
        )
    }
}