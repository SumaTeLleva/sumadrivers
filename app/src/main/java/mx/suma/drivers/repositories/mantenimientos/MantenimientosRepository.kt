package mx.suma.drivers.repositories.mantenimientos

import mx.suma.drivers.models.api.Mantenimiento
import org.json.JSONObject

interface MantenimientosRepository {
    suspend fun bajarPendientesMantenimiento(usuarioId: Long, desde: String, hasta: String): List<Mantenimiento>

    suspend fun guardarMantenimiento(payload: JSONObject): Mantenimiento
}