package apps.dcoder.easysftp.di.modules

import apps.dcoder.easysftp.services.StorageDiscoveryService
import org.koin.dsl.module

val serviceModule = module {
    single { StorageDiscoveryService() }
}