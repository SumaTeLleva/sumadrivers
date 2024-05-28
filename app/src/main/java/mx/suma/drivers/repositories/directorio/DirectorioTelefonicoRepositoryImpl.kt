package mx.suma.drivers.repositories.directorio

import mx.suma.drivers.models.api.Contacto
import mx.suma.drivers.repositories.directorio.remote.DirectorioRemoteDataSource

class DirectorioTelefonicoRepositoryImpl(
    val remoteDataSource: DirectorioRemoteDataSource,
) : DirectorioTelefonicoRepository {

    /*override val contactos: LiveData<List<ContactoModel>>
        get() = directorioDao.getContactos()*/

    /*override suspend fun obtenerDirectorioTelefonico(force: Boolean) = withContext(Dispatchers.IO) {
        if(force || appState.hasDirectorio().not()) {
            Timber.i("Bajando el directorio")
            try {
                directorioDao.cleanAll()
                val result = remoteDataSource.getDirectorio()
                Timber.d("delete all registre db directorio")
                directorioDao.insertAll(*result.asContactoDbModel().toTypedArray())

                appState.marcarCacheDirectorio()

                Timber.d("Directorio bajado con éxito")
            } catch (e: HttpException) {
                throw Exception("Error en este paso")
                Timber.e(e.message())
            }
        } else {
            Timber.i("Existe el directorio en Cache")
        }
    }*/

    override suspend fun obtenerDirectorio():List<Contacto> {
        return remoteDataSource.getDirectorio()
    }
}