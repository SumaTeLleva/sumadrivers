package mx.suma.drivers.repositories.dispositivos

import mx.suma.drivers.models.api.Dispositivo
import org.json.JSONObject

interface DispositivosRepository {
    suspend fun suscribirDispositivo(payload: JSONObject): Dispositivo

    suspend fun actualizarDispositivo(id: Long, payload: JSONObject): Dispositivo
}