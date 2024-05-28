package mx.suma.drivers.repositories.mapas

import mx.suma.drivers.models.api.Destino
import mx.suma.drivers.models.api.Mapa
import mx.suma.drivers.models.api.utils.DirectionResponses
import retrofit2.Response

interface MapasRepository {
    suspend fun getMapa(id: Long): Mapa
    suspend fun getDestination(id: Long): Destino
    suspend fun getDirections(url:String, origin:String, destination:String, key:String): Response<DirectionResponses>
}