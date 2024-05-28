package mx.suma.drivers.repositories.tickets

import mx.suma.drivers.models.api.Ticket
import mx.suma.drivers.repositories.tickets.remote.TicketsRemoteDataSource
import org.json.JSONObject
import java.util.*

class TicketsRepositoryImpl(val remoteDataSource: TicketsRemoteDataSource) : TicketsRepository{
    override suspend fun buscarTicketsPorRangoDeFechas(
        idOperador: Long,
        desde: String,
        hasta: String
    ): List<Ticket> {
        val params = HashMap<String, String>()
        params["id_operador"] = "$idOperador"
        params["fecha=ge"] = desde
        params["fecha=le"] = hasta
        params["sort"] = "sort(-fecha)"

        return remoteDataSource.getTickets(params)
    }

    override suspend fun guardarTicket(payload: JSONObject): Ticket {
        return remoteDataSource.guardarTicket(payload)
    }
}