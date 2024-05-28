package mx.suma.drivers.di

import android.util.Log
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.utils.configuration
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.getKoin
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module(override = true) {
    scope(named("baseURL")) {scoped { provideBaseUrlHolder() }}
    factory { provideRetrofit() }
    single(override = true) { provideSumaApi(get())}
}

fun provideSumaApi(retrofit: Retrofit): ApiSuma = retrofit.create(ApiSuma::class.java)

fun provideRetrofit(): Retrofit {
    val scope = getKoin().getOrCreateScope(
        "baseUrlId", named("baseURL"))
    val baseUrl = scope.getScope("baseUrlId").get<String>()
    Log.d("URL del path","$baseUrl")
    return Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .build()
}

fun provideBaseUrlHolder() = configuration.baseURL