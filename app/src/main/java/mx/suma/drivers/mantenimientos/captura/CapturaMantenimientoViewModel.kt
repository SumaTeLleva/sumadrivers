package mx.suma.drivers.mantenimientos.captura

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mx.suma.drivers.models.db.MantenimientoModel
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.models.db.getPayload
import mx.suma.drivers.repositories.mantenimientos.MantenimientosRepository
import mx.suma.drivers.utils.formatAsDateTimeString
import mx.suma.drivers.utils.now
import org.json.JSONObject
import timber.log.Timber

class CapturaMantenimientoViewModel(
    val usuarioModel: UsuarioModel,
    val repository: MantenimientosRepository
) : ViewModel() {

    val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _mantenimiento = MantenimientoModel(
        fecha = now().time.formatAsDateTimeString(),
        idUnidad = usuarioModel.idUnidad,
        idUsuario = usuarioModel.id
    )

    val data = MutableLiveData<MantenimientoModel>()

    val mantenimientoGenerado = MutableLiveData(-1L)

    val mantenimientoValido = Transformations.map(data) {
        it.titulo.isNotEmpty() && it.notas.isNotEmpty()
    }

    private val _isSendingRequest = MutableLiveData<Boolean>(false)
    val isSendingRequest: LiveData<Boolean>
        get() = _isSendingRequest

    private val _controlsEnabled = MediatorLiveData<Boolean>()
    val controlsEnabled: LiveData<Boolean>
        get() = _controlsEnabled

    private val _failedRequest = MutableLiveData<Boolean>(false)
    val failedRequest: LiveData<Boolean>
        get() = _failedRequest

    init {
        _controlsEnabled.addSource(isSendingRequest) {
            _controlsEnabled.value = combineLatest(isSendingRequest, mantenimientoValido)
        }

        _controlsEnabled.addSource(mantenimientoValido) {
            _controlsEnabled.value = combineLatest(isSendingRequest, mantenimientoValido)
        }
    }

    private fun combineLatest(sending: LiveData<Boolean>, valid: LiveData<Boolean>): Boolean {
        return if (sending.value == false && valid.value == true) {
            true
        } else if (sending.value == true && valid.value == true) {
            false
        } else {
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun setTitulo(value: String) {
        _mantenimiento.titulo = value
        data.value = _mantenimiento
    }

    fun setNotas(value: String) {
        _mantenimiento.notas = value
        data.value = _mantenimiento
    }

    fun onGuardarMantenimiento() {
        uiScope.launch {
            _isSendingRequest.value = true
            _failedRequest.value = false

            try {
                val result =
                    repository.guardarMantenimiento(data.value?.getPayload() as JSONObject)

                if (result.data.id != -1L) {
                    mantenimientoGenerado.value = result.data.id
                }
            } catch (e: Exception) {
                Timber.i("Algo no salió bien ${e.message}")
                _failedRequest.value = true
            } finally {
                _isSendingRequest.value = false
            }
        }
    }

    fun onNavigationComplete() {
        mantenimientoGenerado.value = -1L
    }
}
