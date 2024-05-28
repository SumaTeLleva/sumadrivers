package mx.suma.drivers.repositories.bitacoras

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.database.BitacorasDao
import mx.suma.drivers.models.api.Bitacora
import mx.suma.drivers.models.api.BitacoraDate
import mx.suma.drivers.models.api.utils.asBitacoraDbModel
import mx.suma.drivers.models.db.BitacoraModel
import mx.suma.drivers.repositories.bitacoras.remote.BitacorasRemoteDataSource
import mx.suma.drivers.utils.formatAsDateTimeString
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.util.*

class BitacorasRepositoryImpl(val remoteDataSource: BitacorasRemoteDataSource, val bitacoraDao: BitacorasDao) : BitacorasRepository {

    override suspend fun getBitacorasDb():List<BitacoraModel> {
        return withContext(Dispatchers.IO) {
            bitacoraDao.getBitacoras()
        }
    }

    override suspend fun guardarBitacorasDb(result:List<Bitacora>) {
        withContext(Dispatchers.IO) {
            try {
                bitacoraDao.cleanAll()
                bitacoraDao.insertAll(*result.asBitacoraDbModel().toTypedArray())
            } catch (e: HttpException) {
                Timber.e(e.message())
            }
        }
    }

    override suspend fun buscarBitacoras(
        idProveedor: Long,
        idOperador: Long,
        desde: Date,
        hasta: Date
    ): List<Bitacora> {
        return remoteDataSource.buscarServicios(
            idProveedor, idOperador, desde.formatAsDateTimeString(), hasta.formatAsDateTimeString()
        )
    }

    override suspend fun buscarUltimoServicio(idProveedor: Long, idOperador: Long): List<Bitacora> {
        return remoteDataSource.buscarUltimoServicio(idProveedor, idOperador)
    }

    override suspend fun buscarBitacora(id: Long): Bitacora {
        return remoteDataSource.buscarBitacora(id)
    }

    override suspend fun obtenerFechaActual(): BitacoraDate {
        return remoteDataSource.obtenerFechaActual()
    }

    override suspend fun guardarBitacora(id: Long, payload: JSONObject): Bitacora {
        return remoteDataSource.guardarBitacora(id, payload)
    }

    override suspend fun enviarDatosHuellas(id: Long, payload: JSONObject): ResponseBody {
        return remoteDataSource.guardarHuellas(id, payload)
    }
    override suspend fun buscarBitacoraByIdDb(id:Long): BitacoraModel {
        return withContext(Dispatchers.IO) {
            bitacoraDao.getBitacorasById(id)
        }
    }

    override suspend fun guardarBitacoraDb(bitacora: BitacoraModel) {
        return withContext(Dispatchers.IO) {
            bitacoraDao.insert(bitacora)
        }
    }

    override suspend fun actualizarBitacoraDb(bitacora: BitacoraModel) {
        return withContext(Dispatchers.IO) {
            bitacoraDao.update(bitacora)
        }
    }
}