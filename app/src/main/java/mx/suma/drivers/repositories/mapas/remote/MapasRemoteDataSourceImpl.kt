package mx.suma.drivers.repositories.mapas.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.Destino
import mx.suma.drivers.models.api.Mapa
import mx.suma.drivers.models.api.utils.DirectionResponses
import mx.suma.drivers.network.ApiSuma
import retrofit2.Response

class MapasRemoteDataSourceImpl(val apiSuma: ApiSuma): MapasRemoteDataSource {
    override suspend fun getMapa(id: Long): Mapa {
        return apiSuma.getMapa(id)
    }

    override suspend fun getDestination(params: HashMap<String, String>): Destino {
        return withContext(Dispatchers.IO) {
            apiSuma.getTallers(params)
        }
    }

    override suspend fun getDirections(url:String, params: HashMap<String, String>): Response<DirectionResponses> {
        return withContext(Dispatchers.IO) {
            apiSuma.getDirections(url, params)
        }
    }
}