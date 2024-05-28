package mx.suma.drivers.encuestaCovid

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import mx.suma.drivers.database.UsuarioDao
import mx.suma.drivers.models.api.encuestas.Encuesta
import mx.suma.drivers.models.api.utils.asDbModel
import mx.suma.drivers.models.api.utils.asPreguntaModel
import mx.suma.drivers.models.db.*
import mx.suma.drivers.repositories.encuestas.EncuestasRepository
import mx.suma.drivers.session.AppState
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.now
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception

class EncuestaCovidViewModel(val encuestaId: Long, val encuestasRepository: EncuestasRepository, val appState: AppState, val usuarioDao: UsuarioDao) : ViewModel() {
    val viewModelJob = Job()
    val scope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val encuesta = MutableLiveData<EncuestaModel>()

    val encuestaCovid = MutableLiveData<EncuestaCovid>()

    val comenzarEncuesta = MutableLiveData<Boolean>(true)

    val encuestaIniciada = Transformations.map(comenzarEncuesta) { it.not() }

    val parActual = MutableLiveData<ParPreguntas>()

    val totalContestadas = Transformations.map(encuestaCovid) {
        "Preguntas (${it.preguntasContestadas()}/${it.total})"
    }

    val estatus = MutableLiveData(EstatusLCE.LOADING)

    val mensajeResultadoEncuesta = MutableLiveData("")

    val mensajeRecibido = Transformations.map(mensajeResultadoEncuesta) {
        it.isNotEmpty()
    }

    val accionResultadoEncuesta = MutableLiveData("")

    val navigateToPanel = MutableLiveData(false)

    fun obtenerEncuesta(usuario: UsuarioModel) {
        scope.launch {
            estatus.value = EstatusLCE.LOADING
            try {
                val result = encuestasRepository.getEncuesta(encuestaId)

                encuesta.value = result.asDbModel()

                obtenerPreguntas(result, usuario)

                Timber.d("Se bajó la encuesta, yeiii :)")

            } catch (e: Exception) {
                Timber.e("No se obtuvieron datos de la encuesta ;( ${e.message}")
                estatus.value = EstatusLCE.ERROR
            }
        }
    }

    fun obtenerPreguntas(encuesta: Encuesta, usuario: UsuarioModel) {
        scope.launch {
            try {
                val result = encuestasRepository.getPreguntas(encuesta)

                if(result.isNotEmpty()) {
                    Timber.d("Tenemos las preguntas de la encuesta. Son ${result.size} en total")
                    val chunks = result.asPreguntaModel().chunked(2)
                        .map {
                            ParPreguntas(it, usuario, encuesta.asDbModel())
                        }

                    Timber.d("Total pairs: ${chunks.size}")

                    encuestaCovid.value = EncuestaCovid(chunks, result.size, encuesta.asDbModel(), usuario)

                    encuestaCovid.value?.siguienteParPreguntas()

                    parActual.value = encuestaCovid.value?.siguienteParPreguntas()

                    Timber.d("Fin")

                    estatus.value = EstatusLCE.CONTENT
                } else {
                    Timber.d("Esto está raro, esta encuesta al parecer no tiene preguntas")
                    estatus.value = EstatusLCE.NO_CONTENT
                }

            } catch (e: Exception) {
                Timber.e("Algo salió mal al querer obtener las preguntas ;(")
                Timber.e(e)
                estatus.value = EstatusLCE.ERROR
            }
        }
    }

    fun onComenzarEncuesta() {
        comenzarEncuesta.value = false
    }

    fun onContestarPregunta(id: Long, valor: Int) {
        Timber.d("Pregunta contestada id: $id, valor: $valor")

        encuestaCovid.value?.constestarPregunta(id, valor)

        encuestaCovid.value = encuestaCovid.value
        parActual.value = encuestaCovid.value?.parActual()
    }

    fun onSiguienteParPreguntas() {
        encuestaCovid.value?.siguienteParPreguntas()

        encuestaCovid.value = encuestaCovid.value
        parActual.value = encuestaCovid.value?.parActual()
    }

    fun onEnviarEncuesta() {
        Timber.d("Enviar datos al servidor")

        // enviar a servidor
        scope.launch {
            estatus.value = EstatusLCE.LOADING
            try {
                val result = encuestasRepository.procesarEncuesta(
                    encuesta.value as EncuestaModel, encuestaCovid.value?.getPayload() as JSONObject)

                Timber.d("Encuesta enviada")

                mensajeResultadoEncuesta.value = result.mensaje
                accionResultadoEncuesta.value = result.accion
                appState.setFechaUltimaEncuesta(result.fecha)

                when(result.accion) {
                    "block" -> {
                        Timber.d("Bloquear acceso a Usuario y mostrar mensaje de llamar a coordinador")

                        encuestaCovid.value?.usuario?.let {
                            it.acceso = false

                            withContext(Dispatchers.IO) {
                                usuarioDao.insert(it)
                            }
                        }

                        Timber.d("Se bloquea acceso de usuario")
                    }
                    "none" -> {
                        Timber.d("Mostrar mensaje de agradecimiento")
                    }
                }

                // TODO: Guardar registro de ultima fecha de encuesta registrada y ponderada en Preferences
                estatus.value = EstatusLCE.CONTENT

                //validar acceso
            } catch (e: Exception) {
                Timber.e(e)
                estatus.value = EstatusLCE.ERROR
            }
        }
    }

    fun onNavigateToPanel() {
        navigateToPanel.value = true
    }

    fun onNavigationComplete() {
        navigateToPanel.value = false
    }
}

class ParPreguntas(val preguntas: List<PreguntaModel>, val usuario: UsuarioModel, val encuesta: EncuestaModel) {
    var q: Pair<PreguntaModel,PreguntaModel>
    var r: Pair<RespuestaModel, RespuestaModel>

    init {
        when(preguntas.size) {
            2 -> {
                val q1 = preguntas.first()
                val q2 = preguntas.last()

                q = Pair(q1, q2)

                val r1 = RespuestaModel(-1, usuario.id, encuesta.id, q1.id, null, -2)
                val r2 = RespuestaModel(-1, usuario.id, encuesta.id, q2.id, null, -2)

                r = Pair(r1,r2)

            }
            1 -> {
                val q1 = preguntas.first()
                val q2 = makePreguntaInvalida()

                q = Pair(q1, q2)

                val r1 = RespuestaModel(-1, usuario.id, encuesta.id, q1.id, null, -2)
                val r2 = RespuestaModel(-1, usuario.id, encuesta.id, q2.id, null, -2)

                r = Pair(r1,r2)

            }
            else -> {
                throw Exception("Los argumentos no son correctos, la lista de preguntas debe ser de tamaño 1 o 2 en total")
            }
        }
    }

    fun yaFueContestado(): Boolean {
        if(this.esParIncompleto() && this.r.first.respuesta >= 0) {
            return true
        }

        return when(this.r.first.respuesta + this.r.second.respuesta) {
            2, 1, 0 -> true
            else -> false
        }
    }

    fun marcarContestada(idPregunta: Long, value: Int) {
        when {
            this.q.first.id.equals(idPregunta) -> {
                val r = this.r.first
                r.respuesta = value
                r.contestadaAt = now()

                this.r = Pair(r, this.r.second)
            }
            this.q.second.id.equals(idPregunta) -> {
                val r = this.r.second
                r.respuesta = value
                r.contestadaAt = now()

                this.r = Pair(this.r.first, r)
            }
            else -> {
                Timber.d("WTF")
            }
        }
    }

    fun esParIncompleto(): Boolean {
        return this.q.first.id != -1L && this.q.second.id == -1L
    }

    fun totalContestadas(): Int {
        var total = 0

        if(this.r.first.respuesta >= 0) { total += 1 }

        if(this.r.second.respuesta >= 0) { total += 1 }

        return total
    }
}

class EncuestaCovid(paresPreguntas: List<ParPreguntas>, val total: Int, val encuesta: EncuestaModel, val usuario: UsuarioModel) {
    val preguntasMap = HashMap<Long, Int>()
    val preguntasEncuesta = paresPreguntas

    var current = 0

    init {
        preguntasEncuesta.mapIndexed { index, parPreguntas ->
            preguntasMap[parPreguntas.q.first.id] = index
            preguntasMap[parPreguntas.q.second.id] = index
        }
    }

    fun constestarPregunta(idPregunta: Long, valor: Int) {

        preguntasMap[idPregunta]

        preguntasEncuesta[preguntasMap[idPregunta] as Int].marcarContestada(idPregunta, valor)
    }

    fun parActual(): ParPreguntas {
        return preguntasEncuesta[current]
    }

    // Obtener próximo par de preguntas
    // Es el siguiente indice luego del par antes contestado
    fun siguienteParPreguntas(): ParPreguntas {
        val contestadas = preguntasEncuesta.filter { it.yaFueContestado() }

        return try {
            Timber.d("${contestadas.lastIndex + 1}")

            if (fueFinalizada()) {
                preguntasEncuesta[current]
            } else {
                val next = contestadas.lastIndex + 1
                current = next

                preguntasEncuesta[next]
            }
        } catch (e: Exception) {
            ParPreguntas(listOf(makePreguntaInvalida(), makePreguntaInvalida()), UsuarioModel(), EncuestaModel())
        }
    }

    fun fueFinalizada(): Boolean {
        return current == preguntasEncuesta.lastIndex
    }

    fun esValida(): Boolean {
        return preguntasContestadas() == total
    }

    fun preguntasContestadas(): Int {
        return preguntasEncuesta.map {
            it.totalContestadas()
        }.reduce { acc, i -> acc + i }
    }

    fun getPayload(): JSONObject {
        val payload = JSONObject()
        val respuestasPayload = JSONArray()

        preguntasEncuesta.flatMap {
            listOf(it.r.first, it.r.second)
        }.filter {
            it.respuesta != -2
        }.forEach {
            respuestasPayload.put(it.getPayload())
        }

        payload.put("usuario", usuario.id)
        payload.put("encuesta", encuesta.id)
        payload.put("respuestas", respuestasPayload)

        return payload
    }
}