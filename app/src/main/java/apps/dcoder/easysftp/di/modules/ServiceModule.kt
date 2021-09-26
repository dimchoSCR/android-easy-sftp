package apps.dcoder.easysftp.di.modules

import apps.dcoder.easysftp.filemanager.LocalFileManager
import apps.dcoder.easysftp.filemanager.RemoteFileManager
import apps.dcoder.easysftp.services.storage.SharedPrefsStorageService
import apps.dcoder.easysftp.services.storage.StorageDiscoveryService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val serviceModule = module {
    single { StorageDiscoveryService(androidContext()) }
    single { SharedPrefsStorageService(androidContext()) }
    factory { (rootDirPath: String) ->
        if (rootDirPath.contains('@')) {
            RemoteFileManager()
        } else {
            LocalFileManager(rootDirPath)
        }
    }
}