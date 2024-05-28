package mx.suma.drivers.models.db.validators

import mx.suma.drivers.models.db.TicketModel


class TicketValidator {
    companion object {

        fun isValid(ticketModel: TicketModel): Boolean {

            val result = arrayListOf<Boolean>()
            result.add(validarGasolinera(ticketModel.idGasolinera))

            return result.reduce { acc, b -> acc && b }
        }

        fun validarGasolinera(id: Long): Boolean {
            return id != -1L
        }
    }
}