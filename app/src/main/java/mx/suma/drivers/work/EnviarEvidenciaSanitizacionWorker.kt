package mx.suma.drivers.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import mx.suma.drivers.R
import mx.suma.drivers.network.ApiSuma
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.File


@KoinApiExtension
class EnviarEvidenciaSanitizacionWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params), KoinComponent {

    private val apiSuma: ApiSuma by inject()

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Sanitizaciones worker Start")

            val dir = getOutputDirectory()
            val fs = dir.listFiles()

            fs?.let { fileList ->
                if(fileList.isEmpty()) {
                    Timber.d("Nothing to do")
                } else {
                    Timber.d("We have something to send")

                    fileList.map {
                        Timber.d("Sending file: ${it.name}")

                        val mType = if (it.name.contains("AUDIO")) "audio/mp4" else "video/mp4"

                        val mediaType =  MediaType.parse(mType)
                        val bodyFile = RequestBody.create(mediaType, it)

                        val fileToUpload = MultipartBody.Part.createFormData("evidencia", it.name, bodyFile)

                        val desc = RequestBody.create(MediaType.parse("text/plain"), "Evidencia de sanitización")

                        apiSuma.postEvidencia(desc, fileToUpload)

                        it.delete()

                        Timber.d("File has been sent")
                    }
                }
            }

            Timber.d("Sanitizaciones worker End")

            Result.success()
        } catch (e: Exception) {
            Timber.e(e)

            Result.retry()
        }
    }

    fun getOutputDirectory(): File {
        val mediaDir = applicationContext.externalMediaDirs.firstOrNull()?.let {
            File(it, applicationContext.resources.getString(R.string.app_name)).apply { mkdirs() } }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else applicationContext.filesDir
    }
}