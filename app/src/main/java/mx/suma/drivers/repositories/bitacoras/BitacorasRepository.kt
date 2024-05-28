package mx.suma.drivers.repositories.bitacoras

import mx.suma.drivers.models.api.Bitacora
import mx.suma.drivers.models.api.BitacoraDate
import mx.suma.drivers.models.db.BitacoraModel
import okhttp3.ResponseBody
import org.json.JSONObject
import java.util.*

interface BitacorasRepository {

    suspend fun getBitacorasDb():List<BitacoraModel>
    suspend fun guardarBitacorasDb(result: List<Bitacora>)
    suspend fun buscarBitacoras(idProveedor: Long, idOperador:Long, desde: Date, hasta: Date): List<Bitacora>
    suspend fun buscarBitacora(id: Long): Bitacora
    suspend fun obtenerFechaActual(): BitacoraDate
    suspend fun buscarUltimoServicio(idProveedor: Long, idOperador:Long): List<Bitacora>
    suspend fun guardarBitacora(id: Long, payload: JSONObject): Bitacora
    suspend fun enviarDatosHuellas(id: Long, payload: JSONObject): ResponseBody
    suspend fun buscarBitacoraByIdDb(id: Long): BitacoraModel
    suspend fun guardarBitacoraDb(bitacora: BitacoraModel)
    suspend fun actualizarBitacoraDb(bitacora: BitacoraModel)
}