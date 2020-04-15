package apps.dcoder.easysftp.di

import apps.dcoder.easysftp.di.modules.repoModule
import apps.dcoder.easysftp.di.modules.serviceModule
import apps.dcoder.easysftp.di.modules.viewModelsModule

val allModules = listOf(serviceModule, viewModelsModule, repoModule)

