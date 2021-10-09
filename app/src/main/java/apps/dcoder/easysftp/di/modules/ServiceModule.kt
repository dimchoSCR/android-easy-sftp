package apps.dcoder.easysftp.di.modules

import apps.dcoder.easysftp.filemanager.local.LocalFileManager
import apps.dcoder.easysftp.filemanager.remote.RemoteFileManager
import apps.dcoder.easysftp.services.android.FileManagerType
import apps.dcoder.easysftp.services.storage.SharedPrefsStorageService
import apps.dcoder.easysftp.services.storage.StorageDiscoveryService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val serviceModule = module {
    single { StorageDiscoveryService(androidContext()) }
    single { SharedPrefsStorageService(androidContext()) }
    factory { (fileManagerType: FileManagerType, rootDirPath: String) ->
        if (fileManagerType == FileManagerType.REMOTE) {
            RemoteFileManager(rootDirPath)
        } else {
            LocalFileManager(rootDirPath)
        }
    }
}