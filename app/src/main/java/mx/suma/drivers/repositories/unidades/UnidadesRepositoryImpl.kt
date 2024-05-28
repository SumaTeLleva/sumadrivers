package mx.suma.drivers.repositories.unidades

import mx.suma.drivers.models.api.CambioUnidad
import mx.suma.drivers.models.api.KilometrajeUnidad
import mx.suma.drivers.models.api.ResSaveNewOdometro
import mx.suma.drivers.models.api.Unidad
import mx.suma.drivers.models.api.Unidades
import mx.suma.drivers.repositories.unidades.remote.UnidadesRemoteDataSource
import mx.suma.drivers.session.AppState
import org.json.JSONObject

class UnidadesRepositoryImpl(val appState: AppState, val remoteDataSource: UnidadesRemoteDataSource) : UnidadesRepository {

    /*override val unidades: LiveData<List<UnidadModel>>
        get() = unidadDao.getUnidades()

    override suspend fun bajarUnidadesSumaActivas(force: Boolean) {
        if(force || appState.hasCacheUnidades().not()) {
            Timber.i("Bajando datos de Unidades")
            try {
                withContext(Dispatchers.IO) {
                    val params = HashMap<String, String>()
                    params["activa"] = "true"
                    params["sort"] = "sort(+id)"

                    val result = remoteDataSource.buscarUnidades(params)
                    unidadDao.deleteAll()
                    unidadDao.insertAll(*result.asUnidadDbModel().toTypedArray())

                    appState.marcarCacheUnidades()
                }
            } catch (e: HttpException) {
                Timber.e(e.message())
            }
        } else {
            Timber.i("Existen datos en cache de Unidades")
        }
    }*/

    override suspend fun buscarUnidadesDisponibles():Unidades {
        return remoteDataSource.buscarUnidadesDisponibles()
    }

    // TODO: Usar el cache de unidades en caso de estar disponible
    override suspend fun bajarUnidad(id: Long): Unidad {
        return remoteDataSource.buscarUnidad(id)
    }

    override suspend fun cambiarUnidad(id: Long, payload: JSONObject): CambioUnidad {
        return remoteDataSource.cambiarUnidad(id, payload)
    }

    override suspend fun obtenerKilometrajePorUnidad(id: Long): KilometrajeUnidad {
        val params = HashMap<String, String>()
        params["idUnidad"] = id.toString()
        return remoteDataSource.obtenerKilometrajePorUnidad(params)
    }

    override suspend fun guardarNuevoKilometraje(idUnidad:Long, value:Long): ResSaveNewOdometro {
        val params = HashMap<String, String>()
        params["unidad"] = idUnidad.toString()
        params["nuevo_valor"] = value.toString()
        return remoteDataSource.guardarNuevoKilometraje(params)
    }
}