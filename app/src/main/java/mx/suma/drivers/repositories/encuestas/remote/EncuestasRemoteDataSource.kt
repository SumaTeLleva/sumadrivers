package mx.suma.drivers.repositories.encuestas.remote

import mx.suma.drivers.models.api.BitacoraDate
import mx.suma.drivers.models.api.encuestas.Encuesta
import mx.suma.drivers.models.api.encuestas.EncuestaUnidad
import mx.suma.drivers.models.api.encuestas.Pregunta
import mx.suma.drivers.models.api.encuestas.Respuesta
import mx.suma.drivers.models.api.encuestas.ResultEncuesta
import mx.suma.drivers.models.api.encuestas.ResultEncuestaUnidad
import mx.suma.drivers.models.db.EncuestaModel
import org.json.JSONObject

interface EncuestasRemoteDataSource {
    suspend fun getEncuesta(id: Long): Encuesta

    suspend fun getPreguntas(params: HashMap<String, String>): List<Pregunta>

    suspend fun getRespuestas(params: HashMap<String, String>): List<Respuesta>

    suspend fun postRespuesta(payload: JSONObject): Respuesta

    suspend fun postProcesarEncuesta(encuesta: EncuestaModel, payload: JSONObject): ResultEncuesta

    suspend fun getEncuestaUnidad(): EncuestaUnidad

    suspend fun postEncuestaUnidad(request: JSONObject): ResultEncuestaUnidad

    suspend fun obtenerFechaActual(): BitacoraDate
}