package mx.suma.drivers.repositories.operadores

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.repositories.operadores.remote.OperadoresRemoteDataSource
import okhttp3.ResponseBody

class OperadoresRepositoryImpl(val remoteDataSource: OperadoresRemoteDataSource) : OperadoresRepository {
    override suspend fun generarVisitaOficina(idOperador: Long): ResponseBody {
        return withContext(Dispatchers.IO) {
            remoteDataSource.postVisitaOficina(idOperador)
        }
    }

    override suspend fun generarVisitaTaller(idOperador: Long): ResponseBody {
        return withContext(Dispatchers.IO) {
            remoteDataSource.postVisitaTaller(idOperador)
        }
    }
}