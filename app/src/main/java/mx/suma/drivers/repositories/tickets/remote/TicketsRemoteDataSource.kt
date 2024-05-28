package mx.suma.drivers.repositories.tickets.remote

import mx.suma.drivers.models.api.Ticket
import org.json.JSONObject

interface TicketsRemoteDataSource {
    suspend fun getTickets(params: Map<String, String>): List<Ticket>
    suspend fun guardarTicket(payload: JSONObject): Ticket
}