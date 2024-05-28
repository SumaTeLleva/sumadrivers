package mx.suma.drivers.repositories.dispositivos.remote

import mx.suma.drivers.models.api.Dispositivo
import org.json.JSONObject

interface DispositivosRemoteDataSource {
    suspend fun crearDispositivo(payload: JSONObject): Dispositivo

    suspend fun modificarDispositivo(id:Long, payload: JSONObject): Dispositivo
}