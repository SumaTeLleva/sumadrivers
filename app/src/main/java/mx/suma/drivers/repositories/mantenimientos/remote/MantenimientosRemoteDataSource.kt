package mx.suma.drivers.repositories.mantenimientos.remote

import mx.suma.drivers.models.api.Mantenimiento
import org.json.JSONObject


interface MantenimientosRemoteDataSource {
    suspend fun buscarPendientesMantenimiento(params: Map<String, String>): List<Mantenimiento>

    suspend fun guardarMantenimiento(payload: JSONObject): Mantenimiento
}