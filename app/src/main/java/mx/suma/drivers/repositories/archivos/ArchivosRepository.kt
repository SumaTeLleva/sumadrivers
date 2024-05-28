package mx.suma.drivers.repositories.archivos

import mx.suma.drivers.models.api.archivo.Avisos
import mx.suma.drivers.models.db.ImagenModel


interface ArchivosRepository {
    suspend fun obtenerAvisos(): Avisos

    suspend fun subirImagen(request: ImagenModel): Unit
}