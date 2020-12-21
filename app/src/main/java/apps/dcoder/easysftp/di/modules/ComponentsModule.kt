package apps.dcoder.easysftp.di.modules

import apps.dcoder.easysftp.fragments.StorageAddDialogFragment
import apps.dcoder.easysftp.viewmodels.StorageListViewModel
import org.koin.dsl.module

val componentsModule = module {
    factory { StorageAddDialogFragment(get<StorageListViewModel>()) }
}