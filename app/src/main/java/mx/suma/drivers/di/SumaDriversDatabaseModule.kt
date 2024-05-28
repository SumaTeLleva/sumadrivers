package mx.suma.drivers.di

import mx.suma.drivers.database.SumaDriversDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        SumaDriversDatabase.getInstance(androidContext())
    }
}