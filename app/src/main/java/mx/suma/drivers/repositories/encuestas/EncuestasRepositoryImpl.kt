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
import mx.suma.drivers.models.db.getPayload
import mx.suma.drivers.repositories.encuestas.remote.EncuestasRemoteDataSource
import mx.suma.drivers.utils.formatAsDateTimeString
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class EncuestasRepositoryImpl(val remoteDataSource: EncuestasRemoteDataSource) : EncuestasRepository {
    override suspend fun getEncuesta(id: Long): Encuesta {
        return remoteDataSource.getEncuesta(id)
    }

    override suspend fun getPreguntas(encuesta: Encuesta): List<Pregunta> {
        val params = HashMap<String, String>()
        params["encuesta_id"] = encuesta.data.id.toString()

        return remoteDataSource.getPreguntas(params)
    }

    override suspend fun getRespuestas(
        idEncuesta: Long,
        usuarioId: Long,
        desde: Date,
        hasta: Date
    ): List<Respuesta> {
        val params = HashMap<String, String>()
        params["encuesta_id"] = "$idEncuesta"
        params["usuario_id"] = "$usuarioId"
        params["desde=ge"] = desde.formatAsDateTimeString()
        params["hasta=le"] = hasta.formatAsDateTimeString()

        return remoteDataSource.getRespuestas(params)
    }

    override suspend fun postRespuesta(respuesta: RespuestaModel): Respuesta {
        return remoteDataSource.postRespuesta(respuesta.getPayload())
    }

    override suspend fun procesarEncuesta(
        encuesta: EncuestaModel,
        payload: JSONObject
    ): ResultEncuesta {
        return  remoteDataSource.postProcesarEncuesta(encuesta, payload)
    }

    override suspend fun getEncuestaUnidad(): EncuestaUnidad {
        return remoteDataSource.getEncuestaUnidad()
    }

    override suspend fun postEncuestaUnidad(request: RespuestaUnidadModel): ResultEncuestaUnidad {
        return remoteDataSource.postEncuestaUnidad(request.getPayload())
    }

    override suspend fun obtenerFechaActual(): BitacoraDate {
        return remoteDataSource.obtenerFechaActual()
    }
}