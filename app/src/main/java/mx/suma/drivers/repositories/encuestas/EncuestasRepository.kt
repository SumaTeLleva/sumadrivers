package mx.suma.drivers.repositories.encuestas

import mx.suma.drivers.models.api.BitacoraDate
import mx.suma.drivers.models.api.encuestas.Encuesta
import mx.suma.drivers.models.api.encuestas.EncuestaUnidad
import mx.suma.drivers.models.api.encuestas.Pregunta
import mx.suma.drivers.models.api.encuestas.Respuesta
import mx.suma.drivers.models.api.encuestas.ResultEncuesta
import mx.suma.drivers.models.api.encuestas.ResultEncuestaUnidad
import mx.suma.drivers.models.db.EncuestaModel
import mx.suma.drivers.models.db.RespuestaModel
import mx.suma.drivers.models.db.RespuestaUnidadModel
import org.json.JSONObject
import java.util.*

interface EncuestasRepository {
    suspend fun getEncuesta(id: Long): Encuesta

    suspend fun getPreguntas(encuesta: Encuesta): List<Pregunta>

    suspend fun getRespuestas(idEncuesta: Long, usuarioId: Long, desde: Date, hasta: Date): List<Respuesta>

    suspend fun postRespuesta(respuesta: RespuestaModel): Respuesta

    suspend fun procesarEncuesta(encuesta: EncuestaModel, payload: JSONObject): ResultEncuesta

    suspend fun getEncuestaUnidad(): EncuestaUnidad

    suspend fun postEncuestaUnidad(request: RespuestaUnidadModel): ResultEncuestaUnidad

    suspend fun obtenerFechaActual(): BitacoraDate
}