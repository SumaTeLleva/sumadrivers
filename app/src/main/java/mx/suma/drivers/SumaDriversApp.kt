package mx.suma.drivers

import android.app.Application
import io.armcha.debugBanner.DebugBanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.suma.drivers.di.appStateModule
import mx.suma.drivers.di.databaseModule
import mx.suma.drivers.di.networkModule
import mx.suma.drivers.di.provideBaseUrlHolder
import mx.suma.drivers.utils.configuration
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import timber.log.Timber


class SumaDriversApp : Application() {
    val applicationScope = CoroutineScope(Dispatchers.Default)
    lateinit var scope: Scope

    fun startKoins() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@SumaDriversApp)
            modules(listOf(appStateModule, databaseModule))
        }
    }
    private fun delayedInit() {
        println("startkoin ------------------")
        applicationScope.launch {
            Timber.plant(Timber.DebugTree())
            startKoins()
        }
    }

    override fun onCreate() {
        super.onCreate()
        delayedInit()
        if(!configuration.isProduction) {
            DebugBanner.init(this)
        }
    }

    fun loadNetworkModule(){
        println("loadKoint ------------------")
        provideBaseUrlHolder()
        loadKoinModules(networkModule)
        scope =  getKoin().getOrCreateScope("baseUrlId", named("baseURL"))
    }

    fun refreshScope () {
        scope.close()
        scope = getKoin().getOrCreateScope("baseUrlId", named("baseURL"))
        provideBaseUrlHolder()
        stopKoin()
        startKoins()
    }

    fun stopScope () {
        scope.close()
        stopKoin()
    }
}