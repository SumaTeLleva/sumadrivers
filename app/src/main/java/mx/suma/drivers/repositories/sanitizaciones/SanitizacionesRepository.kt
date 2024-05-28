package mx.suma.drivers.repositories.sanitizaciones

import mx.suma.drivers.models.api.Sanitizacion
import org.json.JSONObject
import java.util.*

interface SanitizacionesRepository {
    suspend fun bajarSanitizaciones(idUnidad: Long, desde: Date, hasta: Date): List<Sanitizacion>
    suspend fun guardarSanitizacion(payload: JSONObject): Sanitizacion
}