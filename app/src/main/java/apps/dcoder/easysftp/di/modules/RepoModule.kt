package apps.dcoder.easysftp.di.modules

import apps.dcoder.easysftp.repos.StorageRepository
import org.koin.dsl.module

val repoModule = module {
    single { StorageRepository() }
}