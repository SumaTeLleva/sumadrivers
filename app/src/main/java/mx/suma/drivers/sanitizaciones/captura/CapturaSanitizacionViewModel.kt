package mx.suma.drivers.sanitizaciones.captura

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.suma.drivers.models.api.utils.asClienteModel
import mx.suma.drivers.models.api.utils.asDbModel
import mx.suma.drivers.models.db.BitacoraModel
import mx.suma.drivers.models.db.ClienteModel
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.repositories.bitacoras.BitacorasRepository
import mx.suma.drivers.repositories.clientes.ClientesRepository
import mx.suma.drivers.repositories.sanitizaciones.SanitizacionesRepository
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.formatAsDateTimeString
import mx.suma.drivers.utils.now
import org.json.JSONObject
import timber.log.Timber

class CapturaSanitizacionViewModel(
    val bitacorasRepository: BitacorasRepository,
    val clientesRepository: ClientesRepository,
    val sanitizacionesRepository: SanitizacionesRepository
) : ViewModel() {
    // TODO: Se deben confirmar los datos de la sanitización
    //        Luego debe seleccionarse el tipo de validación a realizar
    //         - Video
    //         - Audio

    val usuario = MutableLiveData<UsuarioModel>()
    val bitacora = MutableLiveData<BitacoraModel>()
    val cliente = MutableLiveData<ClienteModel>()
    val fecha = now()
    val payload = JSONObject()
    var idSanitizacion: Long = -1

    val estatus = MutableLiveData(EstatusLCE.LOADING)

    // Navigation
    val navigateToRecordAudioFragment = MutableLiveData(false)

    val showConfirmDialogRecordVideo = MutableLiveData(false)
    val showConfirmDialogRecordAudio = MutableLiveData(false)

    val navigateToRecordVideoFragment = MutableLiveData(false)
    val navigateToListadoSanitizaciones = MutableLiveData(false)

    init {
        bajarDatosUltimoServicio()
    }

    fun bajarDatosUltimoServicio() {
        viewModelScope.launch {
            estatus.value = EstatusLCE.LOADING

            usuario.value?.let { usuario ->
                val result = bitacorasRepository.buscarUltimoServicio(
                    usuario.idProveedor,
                    usuario.idOperadorSuma
                )

                if (result.isNotEmpty()) {
                    Timber.d("Got data! ${result.first().data.id}")

                    bitacora.value = result.first().asDbModel()

                    cliente.value = clientesRepository.buscarClientesEmpresarialesActivos()
                        .asClienteModel()
                        .filter {
                            it.id == result.first().asDbModel().idCliente
                        }.first()

                    setPayload(usuario)

                    estatus.value = EstatusLCE.CONTENT
                } else {
                    estatus.value = EstatusLCE.NO_CONTENT
                    Timber.d("Couldn't get data ;(")
                }
            }
        }
    }

    private fun setPayload(usuario: UsuarioModel) {
        payload.put("fecha", now().time.formatAsDateTimeString())
        payload.put("tiempo_inicial", now().time.formatAsDateTimeString())
        payload.put("tiempo_final", now().time.formatAsDateTimeString())
        payload.put("cliente", cliente.value?.id as Long)
        payload.put("operador", usuario.idOperadorSuma)
        payload.put("unidad", usuario.idUnidad)
    }

    fun guardarRegistroSanitizacion(video: Boolean = true) {
        viewModelScope.launch {
            estatus.value = EstatusLCE.LOADING

            try {
                val result = sanitizacionesRepository.guardarSanitizacion(payload)
                idSanitizacion = result.data.id

                if (video) {
                    onNavigateToRecordVideoFragment()
                } else {
                    onNavigateToRecordAudioFragment()
                }

                estatus.value = EstatusLCE.CONTENT
            } catch (e: Exception) {
                estatus.value = EstatusLCE.ERROR
                Timber.d("Ocurrió un error: ${e.message}")
            }
        }
    }

    fun onNavigateToRecordAudioFragment() {
        navigateToRecordAudioFragment.value = true
    }

    fun onNavigateToRecordVideoFragment() {
        navigateToRecordVideoFragment.value = true
    }

    fun onCancelarRegistroSanitizacion() {
        navigateToListadoSanitizaciones.value = true
    }

    fun onNavigationComplete() {
        navigateToRecordAudioFragment.value = false
        navigateToRecordVideoFragment.value = false
        navigateToListadoSanitizaciones.value = false
    }

    fun onShowConfirmDialogRecordVideo() {
        showConfirmDialogRecordVideo.value = true
    }

    fun onShowConfirmDialogRecordAudio() {
        showConfirmDialogRecordAudio.value = true
    }

    fun onShowConfirmDialogComplete() {
        showConfirmDialogRecordAudio.value = false
        showConfirmDialogRecordVideo.value = false
    }
}