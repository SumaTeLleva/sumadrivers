package mx.suma.drivers.di

import mx.suma.drivers.session.AppStateImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appStateModule = module {
    single { AppStateImpl(androidContext()) }
}