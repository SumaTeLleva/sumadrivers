package mx.suma.drivers.repositories.proveedores

import mx.suma.drivers.models.api.EstacionGas
import mx.suma.drivers.models.api.Proveedor


interface ProveedoresRepository {
    /*val gasolineras: LiveData<List<ProveedorModel>>

    suspend fun bajarGasolineras(force: Boolean = false)*/

    suspend fun obtenerGasolineras():List<Proveedor>

    suspend fun getGasolineras(): EstacionGas
}