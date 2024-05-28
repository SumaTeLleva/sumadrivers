package mx.suma.drivers.repositories.operadores

import okhttp3.ResponseBody

interface OperadoresRepository {
    suspend fun generarVisitaOficina(idOperador: Long): ResponseBody

    suspend fun generarVisitaTaller(idOperador: Long): ResponseBody
}