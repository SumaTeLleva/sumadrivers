package mx.suma.drivers.repositories.tickets.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.Ticket
import mx.suma.drivers.network.ApiSuma
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

class TicketsRemoteDataSourceImpl(val apiSuma: ApiSuma) : TicketsRemoteDataSource {
    override suspend fun getTickets(params: Map<String, String>): List<Ticket> =
        withContext(Dispatchers.IO) {
            apiSuma.getTickets(params)
        }

    override suspend fun guardarTicket(payload: JSONObject): Ticket = withContext(Dispatchers.IO) {
        val body = RequestBody.create(MediaType.get("application/json"), payload.toString())
        apiSuma.postTicket(body)
    }
}