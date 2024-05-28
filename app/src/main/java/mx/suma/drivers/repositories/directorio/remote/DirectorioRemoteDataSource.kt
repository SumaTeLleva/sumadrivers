package mx.suma.drivers.repositories.directorio.remote

import mx.suma.drivers.models.api.Contacto

interface DirectorioRemoteDataSource {
    suspend fun getDirectorio(): List<Contacto>
}