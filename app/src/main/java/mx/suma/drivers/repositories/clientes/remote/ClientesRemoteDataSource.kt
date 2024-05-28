package mx.suma.drivers.repositories.clientes.remote

import mx.suma.drivers.models.api.Cliente

interface ClientesRemoteDataSource {
    suspend fun buscarClientes(params: HashMap<String, String>): List<Cliente>
}