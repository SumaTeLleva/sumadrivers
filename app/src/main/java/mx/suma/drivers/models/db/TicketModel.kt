package mx.suma.drivers.models.db

import org.json.JSONObject
import timber.log.Timber

const val CAPTURA_VIA_APP = 0
const val CAPTURA_VIA_WEB = 1

data class TicketModel(
    val id: Long = -1L,
    var fecha: String = "",
    var tipoCombustible: String = "",
    var folio: String = "",
    var monto: Double = 0.toDouble(),
    var litros: Double = 0.toDouble(),
    var precioCombustible: Double = 0.toDouble(),
    var kilometraje: Long = 0,
    var viaCaptura: Int = 1,
    var idUnidad: Long = -1,
    var idOperador: Long = -1,
    var idGasolinera: Long = -1,
    var nombreGasolinera: String = "Sin definir",
    var latitud: Double = 0.toDouble(),
    var longitud: Double = 0.toDouble()
)

fun TicketModel.getPayload(): JSONObject {
    val payload = JSONObject()
    payload.put("fecha", fecha)
    payload.put("unidad", idUnidad)
    payload.put("kilometraje", kilometraje)
    payload.put("operador", idOperador)
    payload.put("folio", folio)
    payload.put("litros", litros)
    payload.put("precio_combustible", precioCombustible)
    payload.put("via_captura", CAPTURA_VIA_WEB)
    payload.put("latitud", latitud)
    payload.put("longitud", longitud)

    if (precioCombustible != 0.toDouble() &&
        litros != 0.toDouble() &&
        kilometraje != 0.toLong()) {

        payload.put("via_captura", CAPTURA_VIA_APP)
    }

    if(idGasolinera != -1L) {
        payload.put("gasolinera", idGasolinera)
    }

    Timber.d(payload.toString())

    return payload
}