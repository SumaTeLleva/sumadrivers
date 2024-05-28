package mx.suma.drivers.repositories.bitacoras.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.Bitacora
import mx.suma.drivers.models.api.BitacoraDate
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.utils.getRequestBodyFromPayload
import okhttp3.ResponseBody
import org.json.JSONObject
import timber.log.Timber

class BitacorasRemoteDataSourceImpl(val apiSuma: ApiSuma) : BitacorasRemoteDataSource {
    // TODO: Refactor, generalize
    override suspend fun buscarServicios(
        idProveedor: Long,
        idOperador: Long,
        desde: String,
        hasta: String
    ): List<Bitacora> {
        val options = HashMap<String, String>()
        options["id_proveedor"] = "$idProveedor"
        options["id_operador"] = "$idOperador"
        options["hora_arranque=ge"] = desde
        options["hora_arranque=le"] = hasta

        Timber.d("$options")

        return withContext(Dispatchers.IO) {
            apiSuma.getBitacoras(options)
        }
    }

    override suspend fun buscarUltimoServicio(idProveedor: Long, idOperador: Long): List<Bitacora> {
        return withContext(Dispatchers.IO) {
            apiSuma.getUltimoServicio(idProveedor, idOperador)
        }
    }

    override suspend fun buscarBitacora(id: Long): Bitacora {
        return withContext(Dispatchers.IO) {
            apiSuma.getBitacora(id)
        }
    }

    override suspend fun obtenerFechaActual(): BitacoraDate {
        return withContext(Dispatchers.IO) {
            apiSuma.getFechaHora()
        }
    }

    override suspend fun guardarBitacora(id: Long, payload: JSONObject): Bitacora {
        return withContext(Dispatchers.IO) {
            apiSuma.putBitacora(id, getRequestBodyFromPayload(payload))
        }
    }

    override suspend fun guardarHuellas(id: Long, payload: JSONObject): ResponseBody {
        return withContext(Dispatchers.IO) {
            apiSuma.postHuellas(id, getRequestBodyFromPayload(payload))
        }
    }
}