package mx.suma.drivers.tickets.listado

import android.widget.TextView
import androidx.databinding.BindingAdapter
import mx.suma.drivers.models.db.TicketModel

@BindingAdapter("datosCargaCombustible")
fun TextView.setDatosCargaCombustible(ticket: TicketModel?) {
    ticket?.let {
        text = "${ticket.litros} lts a \$${ticket.precioCombustible} (\$${ticket.monto})"
    }
}

@BindingAdapter("idUnidadTicket")
fun TextView.setTicketUnidad(ticket: TicketModel?) {
    ticket?.let {
        text = "Unidad ${ticket.idUnidad}"
    }
}

@BindingAdapter("kilometrajeTicket")
fun TextView.setTicketKilometraje(ticket: TicketModel?) {
    ticket?.let {
        text = "Km: ${ticket.kilometraje}"
    }
}

@BindingAdapter("idTicket")
fun TextView.setTicketId(ticket: TicketModel?) {
    ticket?.let {
        text = "Id: ${ticket.id}"
    }
}