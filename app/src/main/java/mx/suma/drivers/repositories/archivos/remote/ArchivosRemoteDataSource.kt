package mx.suma.drivers.repositories.archivos.remote

import mx.suma.drivers.models.api.archivo.Avisos
import mx.suma.drivers.models.db.ImagenModel

interface ArchivosRemoteDataSource {
    suspend fun obtenerAvisos(): Avisos

    suspend fun subirImagen(request: ImagenModel): Unit
}