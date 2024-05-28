package mx.suma.drivers.repositories.directorio.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.Contacto
import mx.suma.drivers.network.ApiSuma

class DirectorioRemoteDataSourceImpl(val apiSuma: ApiSuma) : DirectorioRemoteDataSource {
    override suspend fun getDirectorio(): List<Contacto> = withContext(Dispatchers.IO) {
        apiSuma.getDirectorio()
    }
}