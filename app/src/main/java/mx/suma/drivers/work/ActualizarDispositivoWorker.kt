/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package mx.suma.drivers.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import mx.suma.drivers.BuildConfig
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.dispositivos.DispositivosRepositoryImpl
import mx.suma.drivers.repositories.dispositivos.remote.DispositivosRemoteDataSourceImpl
import mx.suma.drivers.session.AppStateImpl
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.HttpException
import timber.log.Timber


@KoinApiExtension
class ActualizarDispositivoWorker(appContext: Context, params: WorkerParameters) :
        CoroutineWorker(appContext, params), KoinComponent {
    companion object {
        const val WORKER_NAME = "ActualizarDispositivo"
    }

    private val apiSuma: ApiSuma by inject()
    private val appState: AppStateImpl by inject()

    override suspend fun doWork(): Result {
        // TODO: Usar inyección de dependencias
        Timber.i("Actualizando dispositivo")

        val remote = DispositivosRemoteDataSourceImpl(apiSuma)
        val dispositivosRepository = DispositivosRepositoryImpl(remote)

        return try {
            if(appState.getDispositivoId() != -1L) {
                Timber.i("Actualizar dispositivo")

                val lastLocation = appState.getLastKnownLocation()

                val payload = JSONObject()
                payload.put("activo", 1)
                payload.put("latitud", lastLocation["latitud"])
                payload.put("longitud", lastLocation["longitud"])
                payload.put("version_name", BuildConfig.VERSION_NAME)
                payload.put("version_code", BuildConfig.VERSION_CODE)

                dispositivosRepository.actualizarDispositivo(appState.getDispositivoId(), payload)
            }

            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }


}