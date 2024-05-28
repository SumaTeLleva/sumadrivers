package mx.suma.drivers.models.db

import mx.suma.drivers.utils.formatAsDateTimeString
import org.json.JSONObject
import java.util.*

data class RespuestaModel(
    var id: Long,
    var usuarioId: Long,
    var encuestaId: Long,
    var preguntaId: Long,
    var contestadaAt: Calendar?,
    var respuesta: Int
)

fun RespuestaModel.getPayload(): JSONObject {

    val payload = JSONObject()
    payload.put("preguntaId", preguntaId)
    payload.put("contestadaAt", contestadaAt?.time?.formatAsDateTimeString())
    payload.put("respuesta", respuesta)

    return payload
}