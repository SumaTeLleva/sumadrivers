package mx.suma.drivers.repositories.clientes.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.Cliente
import mx.suma.drivers.network.ApiSuma

class ClientesRemoteDataSourceImpl(val apiSuma: ApiSuma) : ClientesRemoteDataSource {
    override suspend fun buscarClientes(params: HashMap<String, String>): List<Cliente> {
        return withContext(Dispatchers.IO) {
            apiSuma.getClientes(params)
        }
    }
}