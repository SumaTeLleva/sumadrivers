package mx.suma.drivers.mantenimientos.listado

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.suma.drivers.models.api.utils.asMantenimientoDbModel
import mx.suma.drivers.models.db.MantenimientoModel
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.repositories.mantenimientos.MantenimientosRepository
import mx.suma.drivers.utils.*

class ListadoMantenimientosViewModel(val mantenimientosRepository: MantenimientosRepository) :
    ViewModel() {

    val estatus = MutableLiveData(EstatusLCE.LOADING)
    val error = MutableLiveData(TypeError.EMPTY)

    val mantenimientos = MutableLiveData<List<MantenimientoModel>>(arrayListOf())

    val navigateCapturaMantenimiento = MutableLiveData(false)


    fun buscarPendientes(usuarioModel: UsuarioModel) {
        viewModelScope.launch {

            estatus.value = EstatusLCE.LOADING

            try {
                val desde = now().startOfYear();
                val hasta = now().endOfYear();

                val result = mantenimientosRepository
                    .bajarPendientesMantenimiento(
                        usuarioModel.id,
                        desde.time.formatAsDateTimeString(), hasta.time.formatAsDateTimeString()
                    )

                if (result.isNotEmpty()) {
                    mantenimientos.value = result.asMantenimientoDbModel()
                    estatus.value = EstatusLCE.CONTENT
                } else {
                    estatus.value = EstatusLCE.NO_CONTENT
                }
            } catch (e: Exception) {
                estatus.value = EstatusLCE.ERROR
                error.value = TypeError.SERVICE
            }
        }
    }

    fun onNavigateToCapturaMantenimiento() {
        navigateCapturaMantenimiento.value = true
    }

    fun onNavigationComplete() {
        navigateCapturaMantenimiento.value = false
    }
}
