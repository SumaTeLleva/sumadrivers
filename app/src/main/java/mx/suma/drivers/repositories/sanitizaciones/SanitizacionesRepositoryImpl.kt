package mx.suma.drivers.repositories.sanitizaciones

import mx.suma.drivers.models.api.Sanitizacion
import mx.suma.drivers.repositories.sanitizaciones.remote.SanitizacionesRemoteDataSource
import mx.suma.drivers.utils.formatAsDateTimeString
import org.json.JSONObject
import java.util.*

class SanitizacionesRepositoryImpl(val remoteDataSource: SanitizacionesRemoteDataSource): SanitizacionesRepository {
    override suspend fun bajarSanitizaciones(
        idUnidad: Long,
        desde: Date,
        hasta: Date
    ): List<Sanitizacion> {
        val params = HashMap<String, String>()
        params["unidad_id"] = "${idUnidad}"
        params["fecha=ge"] = desde.formatAsDateTimeString()
        params["fecha=le"] = hasta.formatAsDateTimeString()
        params["sort"] = "sort(-fecha)"

        return remoteDataSource.buscarSanitizaciones(params)
    }

    override suspend fun guardarSanitizacion(payload: JSONObject): Sanitizacion {
        return remoteDataSource.guardarSanitizacion(payload)
    }
}