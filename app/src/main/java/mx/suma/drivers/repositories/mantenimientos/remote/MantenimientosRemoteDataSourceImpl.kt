package mx.suma.drivers.repositories.mantenimientos.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.Mantenimiento
import mx.suma.drivers.network.ApiSuma
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

class MantenimientosRemoteDataSourceImpl(val apiSuma: ApiSuma) : MantenimientosRemoteDataSource {
    override suspend fun buscarPendientesMantenimiento(params: Map<String, String>): List<Mantenimiento> {
        return withContext(Dispatchers.IO) {
            apiSuma.getMantenimientos(params)
        }
    }

    override suspend fun guardarMantenimiento(payload: JSONObject): Mantenimiento {
        return withContext(Dispatchers.IO) {
            val body = RequestBody.create(
                MediaType.get("application/json"), payload.toString()
            )

            apiSuma.postMantenimiento(body)
        }
    }
}