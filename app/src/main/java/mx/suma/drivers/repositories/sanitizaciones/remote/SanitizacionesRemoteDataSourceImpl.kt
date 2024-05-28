package mx.suma.drivers.repositories.sanitizaciones.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.Sanitizacion
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.utils.getRequestBodyFromPayload
import org.json.JSONObject

class SanitizacionesRemoteDataSourceImpl(val apiSuma: ApiSuma): SanitizacionesRemoteDataSource {
    override suspend fun buscarSanitizaciones(params: HashMap<String, String>): List<Sanitizacion> {
        return withContext(Dispatchers.IO) {
            apiSuma.getSanitizaciones(params)
        }
    }

    override suspend fun guardarSanitizacion(payload: JSONObject): Sanitizacion {
        return withContext(Dispatchers.IO) {
            apiSuma.postSanitizacion(getRequestBodyFromPayload(payload))
        }
    }
}