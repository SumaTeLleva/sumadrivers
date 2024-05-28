package mx.suma.drivers.repositories.directorio

import mx.suma.drivers.models.api.Contacto

interface DirectorioTelefonicoRepository {
//    val contactos: LiveData<List<ContactoModel>>

//    suspend fun obtenerDirectorioTelefonico(force: Boolean = false)
    suspend fun obtenerDirectorio():List<Contacto>
}