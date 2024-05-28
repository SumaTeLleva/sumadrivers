package mx.suma.drivers.repositories.clientes

import mx.suma.drivers.models.api.Cliente

interface ClientesRepository {
    suspend fun buscarClientesEmpresarialesActivos(): List<Cliente>
}