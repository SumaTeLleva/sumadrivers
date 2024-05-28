package mx.suma.drivers.sanitizaciones.listado

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.suma.drivers.models.api.utils.asSanitizacionModel
import mx.suma.drivers.models.db.SanitizacionModel
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.repositories.sanitizaciones.SanitizacionesRepository
import mx.suma.drivers.utils.*
import timber.log.Timber
import java.util.*

class ListadoSanitizacionesViewModel(private val sanitizacionesRepository: SanitizacionesRepository) :
    ViewModel() {

    val usuario = MutableLiveData<UsuarioModel>()
    val error = MutableLiveData(TypeError.EMPTY)
    val sanitizaciones = MutableLiveData<List<SanitizacionModel>>(arrayListOf())

    val proximaSanitizacion = Transformations.map(sanitizaciones) {
        if (it.isNotEmpty()) {
            val nextDate = parseDateTimeString(it.first().fecha as String)
            nextDate.add(Calendar.HOUR, 24 * 15)

            val diff = nextDate.timeInMillis - now().timeInMillis
            val days = (diff / (1000 * 60 * 60 * 24)).toInt()

            "En $days días (${nextDate.time.formatDateAsDateString()})"
        } else {
            "No hay registros en sistema aún"
        }
    }

    val estatus = MutableLiveData(EstatusLCE.LOADING)

    val navigateToCapturaSanitizacion = MutableLiveData(false)

    fun buscarSanitizaciones() {
        viewModelScope.launch {
            estatus.value = EstatusLCE.LOADING

            val desde = now().startOfYear()
            val hasta = now().endOfYear()

            try {
                usuario.value?.let { usuario ->
                    val result =
                        sanitizacionesRepository.bajarSanitizaciones(
                            usuario.idUnidad,
                            desde.time,
                            hasta.time
                        )

                    if (result.isNotEmpty()) {
                        Timber.d("Got something")
                        sanitizaciones.value = result.asSanitizacionModel()
                        estatus.value = EstatusLCE.CONTENT
                    } else {
                        Timber.d("Got nothing yet :(")
                        estatus.value = EstatusLCE.NO_CONTENT
                    }
                }
            } catch (e: Exception) {
                estatus.value = EstatusLCE.ERROR
                error.value = TypeError.SERVICE
            }
        }
    }

    fun onNavigateToCapturaSanitizaciones() {
        navigateToCapturaSanitizacion.value = true
    }

    fun onNavigationComplete() {
        navigateToCapturaSanitizacion.value = false
    }
}