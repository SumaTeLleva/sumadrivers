package mx.suma.drivers.repositories.operadores.remote

import okhttp3.ResponseBody

interface OperadoresRemoteDataSource {
    suspend fun postVisitaOficina(idOperador: Long): ResponseBody

    suspend fun postVisitaTaller(idOperador: Long): ResponseBody
}