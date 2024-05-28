package mx.suma.drivers.models.db

import org.json.JSONObject

data class RespuestaPreguntaModel(
    var idPregunta: Int,
    var idRespuesta: Int
)

fun RespuestaPreguntaModel.getPayload(): JSONObject {
    val payload = JSONObject()
    payload.put("idPregunta", idPregunta)
    payload.put("idRespuesta", idRespuesta)
    return payload
}