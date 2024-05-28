package mx.suma.drivers.repositories.mapas.remote

import mx.suma.drivers.models.api.Destino
import mx.suma.drivers.models.api.Mapa
import mx.suma.drivers.models.api.utils.DirectionResponses
import retrofit2.Response

interface MapasRemoteDataSource {
    suspend fun getMapa(id: Long): Mapa
    suspend fun getDestination(params: HashMap<String, String>): Destino
    suspend fun getDirections(url:String, params: HashMap<String, String>): Response<DirectionResponses>
}