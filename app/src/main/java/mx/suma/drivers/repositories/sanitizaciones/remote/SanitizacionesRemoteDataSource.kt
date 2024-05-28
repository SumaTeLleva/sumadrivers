package mx.suma.drivers.repositories.sanitizaciones.remote

import mx.suma.drivers.models.api.Sanitizacion
import org.json.JSONObject

interface SanitizacionesRemoteDataSource {
    suspend fun buscarSanitizaciones(params: HashMap<String, String>): List<Sanitizacion>
    suspend fun guardarSanitizacion(payload: JSONObject): Sanitizacion
}