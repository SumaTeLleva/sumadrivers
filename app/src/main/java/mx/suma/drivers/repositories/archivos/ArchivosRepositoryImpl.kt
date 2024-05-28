package mx.suma.drivers.repositories.archivos

import mx.suma.drivers.models.api.archivo.Avisos
import mx.suma.drivers.models.db.ImagenModel
import mx.suma.drivers.repositories.archivos.remote.ArchivosRemoteDataSource


class ArchivosRepositoryImpl(val remoteDataSource: ArchivosRemoteDataSource): ArchivosRepository {
    override suspend fun obtenerAvisos(): Avisos {
        return remoteDataSource.obtenerAvisos()
    }

    override suspend fun subirImagen(request: ImagenModel): Unit {
        return remoteDataSource.subirImagen(request)
    }
}