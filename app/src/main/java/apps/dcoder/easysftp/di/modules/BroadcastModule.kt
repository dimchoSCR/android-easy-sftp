package apps.dcoder.easysftp.di.modules

import apps.dcoder.easysftp.broadcast.RemovableMediaStatusBroadcastReceiver
import org.koin.dsl.module

val broadcastModule = module {
    factory { RemovableMediaStatusBroadcastReceiver(get()) }
}