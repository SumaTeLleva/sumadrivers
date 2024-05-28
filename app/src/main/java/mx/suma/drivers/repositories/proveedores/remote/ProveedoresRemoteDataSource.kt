package mx.suma.drivers.repositories.proveedores.remote

import mx.suma.drivers.models.api.EstacionGas
import mx.suma.drivers.models.api.Proveedor

interface ProveedoresRemoteDataSource {
    suspend fun buscarProveedores(params: HashMap<String, String>): List<Proveedor>

    suspend fun getGasolineras(): EstacionGas
}