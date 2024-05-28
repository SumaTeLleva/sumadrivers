package mx.suma.drivers.repositories.tickets


import mx.suma.drivers.models.api.Ticket
import org.json.JSONObject

interface TicketsRepository {
    suspend fun buscarTicketsPorRangoDeFechas(idOperador: Long, desde: String, hasta: String): List<Ticket>
    suspend fun guardarTicket(payload: JSONObject): Ticket
}