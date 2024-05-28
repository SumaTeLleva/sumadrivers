package mx.suma.drivers.bitacoras.testing

import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mx.suma.drivers.models.api.Unidad
import mx.suma.drivers.models.api.encuestas.EncuestaUnidad
import mx.suma.drivers.models.api.encuestas.RespuestaUnidad
import mx.suma.drivers.models.db.ImagenModel
import mx.suma.drivers.models.db.RespuestaUnidadModel
import mx.suma.drivers.repositories.archivos.ArchivosRepository
import mx.suma.drivers.repositories.encuestas.EncuestasRepository
import mx.suma.drivers.repositories.unidades.UnidadesRepository
import mx.suma.drivers.session.AppState
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.TypeError
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TestBitacoraViewModel(
    val appState: AppState,
    val encuestaRepository: EncuestasRepository,
    val unidadesRepository: UnidadesRepository,
    val archivoRepository: ArchivosRepository,
    val unidadId: Int,
    val operadorId: Int
): ViewModel() {
    private val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val datosEncuesta = MutableLiveData<EncuestaUnidad>()
    val status = MutableLiveData(EstatusLCE.LOADING)
    val error = MutableLiveData(TypeError.EMPTY)

    val unidad = MutableLiveData<Unidad>()
    val valueResponse = MutableLiveData<RespuestaUnidad>()
    val valueRespuestaImage = MutableLiveData<ArrayList<RespuestaUnidad.RespuestaImagen>>()

    val imageComodin = MutableLiveData<ImageView>()
    val idPreguntaComodin = MutableLiveData<Int>(0)
    val preguntasRequeridas = MutableLiveData<Int>(0)
    val imagenesRequeridas = MutableLiveData<Int>(0)


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        valueResponse.value = RespuestaUnidad()
        valueRespuestaImage.value = ArrayList()
        getUnidById(unidadId)
        obtenerEncuesta()
    }

    fun setValueResponse(respuesta: RespuestaUnidad.Respuesta) {
        valueResponse.value.let { response ->
            val newlist = response?.listRespuesta?.find { it.idPregunta == respuesta.idPregunta }
            if(newlist != null) {
                val index = response.listRespuesta.indexOf(newlist)
                response.listRespuesta[index] = respuesta

                // Elimina todos los hijos para volver ser seleccionados
                for((index, value) in response.listRespuesta.withIndex()){
                    if(respuesta.idPregunta == value.idParent) {
                        response.listRespuesta.removeAt(index)
                    }
                }
            }else {
                response?.listRespuesta?.add(respuesta)
            }
            valueResponse.value?.listRespuesta = response?.listRespuesta!!
        }
    }

    fun setValueResponseImage( idPregunta:Int, image:File) {
        val listaComodin = valueRespuestaImage.value
        val resp = listaComodin?.find { it.idPregunta == idPregunta }
        if(resp !== null) {
            val index = listaComodin.indexOf(resp)
            listaComodin[index].image = image
        }else {
            listaComodin?.add(RespuestaUnidad.RespuestaImagen(idPregunta, image))
        }

        valueRespuestaImage.value = listaComodin!!
    }

    private fun obtenerEncuesta() {
        uiScope.launch {
            try {
                val response = encuestaRepository.getEncuestaUnidad()
                if(response.data.isNotEmpty()) {
                    status.value = EstatusLCE.CONTENT
                    datosEncuesta.value = response
                    valueResponse.value.let {
                        it?.idUnidad = unidadId
                        it?.idEmpleado = operadorId
                    }
                } else {
                    status.value = EstatusLCE.NO_CONTENT
                }

            } catch (e: Exception) {
                Timber.e(e.message)
                error.value = TypeError.SERVICE
                status.value = EstatusLCE.ERROR
            }
        }
    }

    private fun getUnidById(unidadId:Int) {
        uiScope.launch {
            try {
                val resUnidad = unidadesRepository.bajarUnidad(unidadId.toLong())
                unidad.value = resUnidad
            }catch (e: Exception) {
                println(e.message)
            }
        }
    }

    suspend fun enviarRespuesta() {
        valueResponse.value.let {
            try {
                val response = encuestaRepository.postEncuestaUnidad(RespuestaUnidadModel(it!!))
                coroutineScope {
                    valueRespuestaImage.value?.map {
                        async {
                            archivoRepository.subirImagen(ImagenModel(it.idPregunta, it.image!!, response.id_encuesta))
                        }
                    }?.awaitAll()
                }
            }catch (e:HttpException) {
                Timber.e(e.message())
            }
        }
    }
    suspend fun saveRegistrerTesting() {
        uiScope.launch {
            try {
                val responseDate = encuestaRepository.obtenerFechaActual()
                val date = SimpleDateFormat("yyyy-MM-d", Locale.ROOT).parse(responseDate.data.FECHA_HORA)
                val fechaActual = Calendar.getInstance()
                fechaActual.time = date
                appState.setLastTesting(fechaActual.timeInMillis, unidadId)
            }catch (e:HttpException) {
                println(e.message())
            }
        }
    }
}