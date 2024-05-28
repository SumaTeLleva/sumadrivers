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
import mx.suma.drivers.database.SumaDriversDatabase
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.bitacoras.BitacorasRepositoryImpl
import mx.suma.drivers.repositories.bitacoras.remote.BitacorasRemoteDataSourceImpl
import mx.suma.drivers.utils.FileImageFactory
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.HttpException
import timber.log.Timber
import java.io.File


@KoinApiExtension
class EnviarDatosAforosWorker(appContext: Context, params: WorkerParameters) :
        CoroutineWorker(appContext, params), KoinComponent {

    private val apiSuma: ApiSuma by inject()
    private val database: SumaDriversDatabase by inject()

    override suspend fun doWork(): Result {
        Timber.i("Actualizando dispositivo")

        val remote = BitacorasRemoteDataSourceImpl(apiSuma)
        val bitacoraDao = database.bitacoraDao
        val repository = BitacorasRepositoryImpl(remote, bitacoraDao)

        return try {

            val a = applicationContext.fileList()

            a.map {
                val parts = it.split(".")
                if (parts.contains("bz2")) {
                    Timber.d("Send file: $it")

                    val file = File(applicationContext.filesDir, it)
                    val payload = JSONObject()
                    payload.put("imagenBase64", FileImageFactory.getBase64Image(file))
                    payload.put("fileName", it)

                    val result = repository.enviarDatosHuellas(parts[0].toLong(), payload)

                    Timber.d(result.toString())

                    file.delete()
                }
            }

            Result.success()
        } catch (e: HttpException) {
            Timber.d(e.code().toString())
            Timber.d(e.response()?.errorBody().toString())
            Result.retry()
        }
    }
}