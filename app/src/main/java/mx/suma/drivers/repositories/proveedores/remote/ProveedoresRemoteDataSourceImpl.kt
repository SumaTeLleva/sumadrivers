package mx.suma.drivers.repositories.proveedores.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.EstacionGas
import mx.suma.drivers.models.api.Proveedor
import mx.suma.drivers.network.ApiSuma

class ProveedoresRemoteDataSourceImpl(val apiSuma: ApiSuma) : ProveedoresRemoteDataSource {
    override suspend fun buscarProveedores(params: HashMap<String, String>): List<Proveedor> {
        return apiSuma.getProveedores(params)
    }

    override suspend fun getGasolineras(): EstacionGas {
        return withContext(Dispatchers.IO) {
            apiSuma.getGasolineras()
        }
    }
}