package mx.suma.drivers.repositories.proveedores

import mx.suma.drivers.database.ProveedorDao
import mx.suma.drivers.models.api.EstacionGas
import mx.suma.drivers.models.api.Proveedor
import mx.suma.drivers.repositories.proveedores.remote.ProveedoresRemoteDataSource
import mx.suma.drivers.session.AppState

class ProveedoresRepositoryImpl(
    val appState: AppState,
    val remoteDataSource: ProveedoresRemoteDataSource,
    val proveedorDao: ProveedorDao
) : ProveedoresRepository {

    private val paramsGasolineras: HashMap<String, String>
        get() {
            val params = HashMap<String, String>()
            params["id_categoria"] = "1"
            params["activo"] = "true"
            params["sort"] = "sort(+empresa)"
            return params
        }

    /*override val gasolineras: LiveData<List<ProveedorModel>>
        get() = proveedorDao.getGasolineras()

    override suspend fun bajarGasolineras(force: Boolean) {
        if (force || appState.hasCacheGasolineras().not()) {
            Timber.i("Bajando datos de gasolineras")

            withContext(Dispatchers.IO) {
                try {
                    val params = paramsGasolineras
                    val result = remoteDataSource.buscarProveedores(params)
                    proveedorDao.deleteAll()
                    proveedorDao.insertAll(*result.asProveedorDbModel().toTypedArray())

                    appState.marcarCacheGasolineras()
                } catch (e: HttpException) {
                    Timber.e(e.message())
                }
            }
        } else {
            Timber.i("Existen datos en cache de Gasolineras")
        }
    }*/

    override suspend fun obtenerGasolineras():List<Proveedor> {
        val params = paramsGasolineras
        return remoteDataSource.buscarProveedores(params)
    }

    override suspend fun getGasolineras(): EstacionGas {
        return remoteDataSource.getGasolineras()
    }
}