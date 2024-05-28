package mx.suma.drivers.repositories.encuestas.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.models.api.BitacoraDate
import mx.suma.drivers.models.api.encuestas.Encuesta
import mx.suma.drivers.models.api.encuestas.EncuestaUnidad
import mx.suma.drivers.models.api.encuestas.Pregunta
import mx.suma.drivers.models.api.encuestas.Respuesta
import mx.suma.drivers.models.api.encuestas.ResultEncuesta
import mx.suma.drivers.models.api.encuestas.ResultEncuestaUnidad
import mx.suma.drivers.models.db.EncuestaModel
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.utils.getRequestBodyFromPayload
import org.json.JSONObject


class EncuestasRemoteDataSourceImpl(val apiSuma: ApiSuma): EncuestasRemoteDataSource {
    override suspend fun getEncuesta(id: Long): Encuesta {
        return withContext(Dispatchers.IO) {
            apiSuma.getEncuesta(id)
        }
    }

    override suspend fun getPreguntas(params: HashMap<String, String>): List<Pregunta> {
        return withContext(Dispatchers.IO) {
            apiSuma.getPreguntas(params)
        }
    }

    override suspend fun getRespuestas(params: HashMap<String, String>): List<Respuesta> {
        return withContext(Dispatchers.IO) {
            apiSuma.getRespuestas(params)
        }
    }

    override suspend fun postRespuesta(payload: JSONObject): Respuesta {
        return withContext(Dispatchers.IO) {
            apiSuma.postRespuesta(getRequestBodyFromPayload(payload))
        }
    }

    override suspend fun postProcesarEncuesta(
        encuesta: EncuestaModel,
        payload: JSONObject
    ): ResultEncuesta {
        return withContext(Dispatchers.IO) {
            apiSuma.postProcesarEncuesta(encuesta.id, getRequestBodyFromPayload(payload))
        }
    }

    override suspend fun getEncuestaUnidad(): EncuestaUnidad {
        return withContext(Dispatchers.IO) {
            apiSuma.getEncuestaUnidad()
        }
    }

    override suspend fun postEncuestaUnidad(request: JSONObject): ResultEncuestaUnidad {
        return withContext(Dispatchers.IO) {
            apiSuma.postEncuestaUnidad(getRequestBodyFromPayload(request))
        }
    }

    override suspend fun obtenerFechaActual(): BitacoraDate {
        return withContext(Dispatchers.IO) {
            apiSuma.getFechaHora()
        }
    }
}