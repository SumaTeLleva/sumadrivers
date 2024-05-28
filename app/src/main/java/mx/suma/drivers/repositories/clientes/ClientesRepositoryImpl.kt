package mx.suma.drivers.repositories.clientes

import mx.suma.drivers.models.api.Cliente
import mx.suma.drivers.repositories.clientes.remote.ClientesRemoteDataSource

class ClientesRepositoryImpl(val remoteDataSource: ClientesRemoteDataSource) : ClientesRepository {
    override suspend fun buscarClientesEmpresarialesActivos(): List<Cliente> {
        val params = HashMap<String, String>()
        params["activo"] = "true"
        params["id_categoria"] = "1"

        return remoteDataSource.buscarClientes(params)
    }
}