package mx.suma.drivers.repositories.operadores.remote

import mx.suma.drivers.network.ApiSuma
import okhttp3.ResponseBody

class OperadoresRemoteDataSourceImpl(val apiSuma: ApiSuma) : OperadoresRemoteDataSource {
    override suspend fun postVisitaOficina(idOperador: Long): ResponseBody {
        return apiSuma.postVisitaOficina(idOperador)
    }

    override suspend fun postVisitaTaller(idOperador: Long): ResponseBody {
        return apiSuma.postVisitaTaller(idOperador)
    }
}