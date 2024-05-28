package mx.suma.drivers.repositories.mantenimientos

import mx.suma.drivers.models.api.Mantenimiento
import mx.suma.drivers.repositories.mantenimientos.remote.MantenimientosRemoteDataSource
import org.json.JSONObject

class MantenimientosRepositoryImpl(val remoteDataSource: MantenimientosRemoteDataSource) :
    MantenimientosRepository {
    override suspend fun bajarPendientesMantenimiento(
        usuarioId: Long,
        desde: String,
        hasta: String
    ): List<Mantenimiento> {
        val payload = HashMap<String, String>()
        payload["id_usuario"] = "${usuarioId}"
        payload["fecha=ge"] = desde
        payload["fecha=le"] = hasta
        payload["sort"] = "sort(-fecha)"

        return remoteDataSource.buscarPendientesMantenimiento(payload)
    }

    override suspend fun guardarMantenimiento(payload: JSONObject): Mantenimiento {
        return remoteDataSource.guardarMantenimiento(payload)
    }
}