package mx.suma.drivers.repositories.dispositivos.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.Dispositivo
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.utils.getRequestBodyFromPayload
import org.json.JSONObject

class DispositivosRemoteDataSourceImpl(val apiSuma: ApiSuma) : DispositivosRemoteDataSource {
    override suspend fun crearDispositivo(payload: JSONObject): Dispositivo {
        return withContext(Dispatchers.IO) {
            apiSuma.postDispositivo(getRequestBodyFromPayload(payload))
        }
    }

    override suspend fun modificarDispositivo(id: Long, payload: JSONObject): Dispositivo {
        return withContext(Dispatchers.IO) {
            apiSuma.putDispositivo(id, getRequestBodyFromPayload(payload))
        }
    }
}