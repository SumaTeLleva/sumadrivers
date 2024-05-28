package mx.suma.drivers.models.db

import mx.suma.drivers.models.api.encuestas.RespuestaUnidad
import org.json.JSONArray
import org.json.JSONObject

data class RespuestaUnidadModel(
    var respuesta: RespuestaUnidad
)

fun RespuestaUnidadModel.getPayload(): JSONObject {
    val payload = JSONObject()
    val arrayRespuesta = JSONArray()
    payload.put("idEmpleado", respuesta.idEmpleado)
    payload.put("idUnidad", respuesta.idUnidad)
    respuesta.listRespuesta.forEach{
        arrayRespuesta.put(RespuestaPreguntaModel(it.idPregunta,it.idRespuesta).getPayload())
    }
    payload.put("data", arrayRespuesta)
    return payload
}