package apps.dcoder.easysftp.di.modules

import apps.dcoder.easysftp.viewmodels.StorageListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { StorageListViewModel(get()) }
}