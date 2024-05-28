package mx.suma.drivers.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import mx.suma.drivers.database.SumaDriversDatabase
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.session.AppStateImpl
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

@KoinApiExtension
class ActualizarCacheWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params), KoinComponent {
    private val appState: AppStateImpl by inject()
    private val apiSuma: ApiSuma by inject()
    private val database: SumaDriversDatabase by inject()

    companion object {
        const val WORKER_NAME = "ActualizarCacheWorker"
    }

    override suspend fun doWork(): Result {
        Timber.d("Se inicia actualización del cache de datos")

        return try {

//            val dataSource = DirectorioRemoteDataSourceImpl(apiSuma)
//            val directorioRepository =
//                DirectorioTelefonicoRepositoryImpl(appState, dataSource, database.directorioDao)

//            val unidadesRemoteDataSource = UnidadesRemoteDataSourceImpl(apiSuma)
//            val unidadesRepository = UnidadesRepositoryImpl(appState, unidadesRemoteDataSource, database.unidadDao)

//            val proveedoresRemoteDataSource = ProveedoresRemoteDataSourceImpl(apiSuma)
//            val proveedoresRepository = ProveedoresRepositoryImpl(appState, proveedoresRemoteDataSource, database.proveedorDao)

//            directorioRepository.obtenerDirectorioTelefonico(true)
//            unidadesRepository.bajarUnidadesSumaActivas(true)
//            proveedoresRepository.bajarGasolineras(true)

            Timber.d("Cache de aplicación actualizado con éxito :)")

            Result.success()
        } catch (e: Exception) {
            Timber.d("Error al intentar actualizar el cache de la aplicación ${e.message}")
            Result.failure()
        }
    }
}