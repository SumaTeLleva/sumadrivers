package mx.suma.drivers.miUnidad

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import mx.suma.drivers.database.UsuarioDao
import mx.suma.drivers.models.api.Unidad
import mx.suma.drivers.models.api.utils.asIds
import mx.suma.drivers.models.api.utils.asUnidadDbModel
import mx.suma.drivers.models.api.utils.filterUnidadLibre
import mx.suma.drivers.models.db.UnidadModel
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.repositories.unidades.UnidadesRepository
import mx.suma.drivers.session.AppState
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.TypeError
import mx.suma.drivers.utils.formatAsDateTimeString
import mx.suma.drivers.utils.now
import org.json.JSONObject
import timber.log.Timber

class MiUnidadViewModel(val unidadesRepository: UnidadesRepository, val usuarioDao: UsuarioDao, val appState: AppState,) : ViewModel() {

    val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val usuario = MutableLiveData<UsuarioModel>()

    val currentTime = MutableLiveData<String>()

    val estatus = MutableLiveData(EstatusLCE.LOADING)
    val error = MutableLiveData(TypeError.EMPTY)

    private val _isSendingRequest = MutableLiveData(false)
    val sendingRequest: LiveData<Boolean>
        get() = _isSendingRequest

    val tomarUnidad = MutableLiveData<Boolean>(false)
    val seleccionarUnidad = MutableLiveData<Boolean>(true)

    val _unidades = MutableLiveData<List<Int>>()
    val unidades = Transformations.map(_unidades) {
        it.map { unidad -> "U${unidad}" }
    }

    val unidadActual = MutableLiveData<Long>(-1)
    val unidadActualString = Transformations.map(unidadActual) {
        "Actual: U${it}"
    }

    val unidadSeleccionada = MutableLiveData<Long>(-1L)
    val unidadSeleccionadaString = Transformations.map(unidadSeleccionada) {
        "Cambio: U$it"
    }

    val unidad = MutableLiveData<Unidad>()
    val descripcionUnidad = Transformations.map(unidad) {
        "U${it.data.id} - ${it.attr.descripcion} (${it.attr.placasFederales})"
    }

    val tipoUnidad = Transformations.map(unidad) {
        "${it.attr.tipo} (${it.rel.tipoUnidad.attr.tipoCombustible})"
    }

    val pasajeros = Transformations.map(unidad) {
        "${it.attr.pasajeros} pasajeros"
    }

    val unidadNoValida = MutableLiveData(true)
    val unidadValida = Transformations.map(unidadNoValida) { it.not() }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        //bajarDatosUnidad()
//        bajarUnidades()

        currentTime.value = now().time.formatAsDateTimeString()
    }

    fun bajarDatosUnidad(isChange:Boolean) =
        uiScope.launch {
            estatus.value = EstatusLCE.LOADING
            usuario.value?.let { usuario ->
                try {
                    println("Unidad: ${usuario.idUnidad}");
                    val resultUnidad = unidadesRepository.buscarUnidadesDisponibles().asIds()
                    _unidades.value = resultUnidad
                    if (usuario.idUnidad > 0L) {
                        val result = unidadesRepository.bajarUnidad(usuario.idUnidad)
                        unidad.value = result
                        unidadNoValida.value = false
                        if(isChange) {
                            println("Ultimo km registrado: ${result.attr.kilometraje}")
                            appState.setUltimoKilometraje(result.attr.kilometraje)
                        }
                    }
                    estatus.value = EstatusLCE.CONTENT
                }catch (e:Exception) {
                    Timber.e(e.message)
                    estatus.value = EstatusLCE.ERROR
                    error.value = TypeError.SERVICE
                }
            }
        }

    fun setUnidadSeleccionada(item: String) {
        unidadSeleccionada.value = item.replace("U", "").toLong()
    }

    fun onTomarUnidad() {
        // TODO: Solo si el valor de la unidad seleccionada es diferente al de la actual
        usuario.value?.let {
            if(it.idUnidad != unidadSeleccionada.value) {
                tomarUnidad.value = true
                seleccionarUnidad.value = false
            }
        }
    }

    fun onTomarUnidadFinished() {
        tomarUnidad.value = false
        seleccionarUnidad.value = true
    }

    fun onCambiarUnidad() {
        try {
            Timber.d("Enviar los datos al servidor y actualizar la unidad en el usuario")

            uiScope.launch {
                usuario.value?.let {usuario ->
                    val payload = JSONObject().apply {
                        put("fecha", currentTime.value)
                        put("idUnidadNueva", unidadSeleccionada.value)
                        put("idOperador", usuario.idOperadorSuma)
                        put("latitud", 0)
                        put("longitud", 0)
                    }

                    val result = unidadesRepository.cambiarUnidad(unidadActual.value as Long, payload)
                    unidadSeleccionada.value = -1
                    unidadActual.value = result.unidadActual
                    usuario.idUnidad = result.unidadActual
                    withContext(Dispatchers.IO) {
                        usuarioDao.insert(usuario)
                    }

                    bajarDatosUnidad(true)
                    onTomarUnidadFinished()

                }
            }
        }catch (e:Exception) {
            Timber.d(e)
        }
    }
}
