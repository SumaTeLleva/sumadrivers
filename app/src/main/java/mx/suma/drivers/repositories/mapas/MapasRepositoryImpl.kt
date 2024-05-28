package mx.suma.drivers.repositories.mapas

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.Destino
import mx.suma.drivers.models.api.Mapa
import mx.suma.drivers.models.api.utils.DirectionResponses
import mx.suma.drivers.repositories.mapas.remote.MapasRemoteDataSource
import retrofit2.Response

class MapasRepositoryImpl(val mapasRemoteDataSource: MapasRemoteDataSource): MapasRepository {
    override suspend fun getMapa(id: Long): Mapa {
        return withContext(Dispatchers.IO) {
            mapasRemoteDataSource.getMapa(id)
        }
    }

    override suspend fun getDestination(id: Long): Destino {
        val params = HashMap<String, String>()
        params["idBitacora"] = id.toString()
        return mapasRemoteDataSource.getDestination(params)
    }
    override suspend fun getDirections(url:String, origin:String, destination:String, key:String): Response<DirectionResponses> {
        val params = HashMap<String, String>()
        params["origin"] = origin
        params["destination"] = destination
        params["key"] = key

        return mapasRemoteDataSource.getDirections(url, params)
    }
}