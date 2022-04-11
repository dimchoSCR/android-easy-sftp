package apps.dcoder.easysftp

import android.app.Application
import android.content.Context
import apps.dcoder.easysftp.di.allModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MainApplication)
            modules(allModules)
        }

        appContext = this.applicationContext
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}