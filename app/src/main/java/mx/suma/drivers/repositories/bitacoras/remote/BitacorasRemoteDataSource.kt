package mx.suma.drivers.repositories.bitacoras.remote

import mx.suma.drivers.models.api.Bitacora
import mx.suma.drivers.models.api.BitacoraDate
import okhttp3.ResponseBody
import org.json.JSONObject

interface BitacorasRemoteDataSource {
    suspend fun buscarServicios(
        idProveedor: Long, idOperador: Long, desde: String, hasta: String): List<Bitacora>

    suspend fun buscarBitacora(id: Long): Bitacora

    suspend fun obtenerFechaActual(): BitacoraDate

    suspend fun buscarUltimoServicio(idProveedor: Long, idOperador: Long): List<Bitacora>

    suspend fun guardarBitacora(id:Long, payload: JSONObject): Bitacora

    suspend fun guardarHuellas(id: Long, payload: JSONObject): ResponseBody
}