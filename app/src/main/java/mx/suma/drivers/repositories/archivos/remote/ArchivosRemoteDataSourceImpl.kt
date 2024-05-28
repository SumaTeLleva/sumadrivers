package mx.suma.drivers.repositories.archivos.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.archivo.Avisos
import mx.suma.drivers.models.db.ImagenModel
import mx.suma.drivers.network.ApiSuma
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ArchivosRemoteDataSourceImpl(val apiSuma: ApiSuma): ArchivosRemoteDataSource {
    override suspend fun obtenerAvisos(): Avisos {
        return withContext(Dispatchers.IO) {
            apiSuma.getArchivoAvisos()
        }
    }

    override suspend fun subirImagen(request: ImagenModel): Unit {
        return withContext(Dispatchers.IO) {

            val mediaType =  MediaType.parse("image/jpg")
            val bodyFile = RequestBody.create(mediaType, request.imagen)
            val imagen: MultipartBody.Part = MultipartBody.Part.createFormData("imagen", request.imagen.name, bodyFile)
            val idEncuesta = RequestBody.create(MediaType.parse("text/plain"), request.id_encuesta.toString())
            apiSuma.subirImagen(idEncuesta, imagen)
        }
    }
}