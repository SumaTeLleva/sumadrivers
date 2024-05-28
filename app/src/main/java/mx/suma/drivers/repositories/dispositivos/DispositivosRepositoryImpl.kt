package mx.suma.drivers.repositories.dispositivos

import mx.suma.drivers.models.api.Dispositivo
import mx.suma.drivers.repositories.dispositivos.remote.DispositivosRemoteDataSource
import org.json.JSONObject

class DispositivosRepositoryImpl(val remoteDataSource: DispositivosRemoteDataSource) : DispositivosRepository{
    override suspend fun suscribirDispositivo(payload: JSONObject): Dispositivo {
        return remoteDataSource.crearDispositivo(payload)
    }

    override suspend fun actualizarDispositivo(id: Long, payload: JSONObject): Dispositivo {
        return remoteDataSource.modificarDispositivo(id, payload)
    }
}