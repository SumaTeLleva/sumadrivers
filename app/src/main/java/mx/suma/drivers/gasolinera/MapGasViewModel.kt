package mx.suma.drivers.gasolinera

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mx.suma.drivers.models.api.EstacionGas
import mx.suma.drivers.models.api.utils.DirectionResponses
import mx.suma.drivers.models.db.LocationGasStation
import mx.suma.drivers.repositories.mapas.MapasRepository
import mx.suma.drivers.repositories.proveedores.ProveedoresRepository
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.TypeError
import retrofit2.HttpException
import retrofit2.Response

class MapGasViewModel(
    val proveedorRepository: ProveedoresRepository,
    val mapasRepository: MapasRepository
): ViewModel() {
    val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val listaDeEstacion = MutableLiveData<EstacionGas>()

    val locations = MutableLiveData<LocationGasStation>(null)
    val error = MutableLiveData(TypeError.EMPTY)
    val estatus = MutableLiveData(EstatusLCE.NO_CONTENT)

    init {
        obtenerGasolineras()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun obtenerGasolineras() {
            uiScope.launch {
                try {
                    val result = proveedorRepository.getGasolineras()
                    listaDeEstacion.value = result
                }catch (e:HttpException) {
                    when(e.code()){
                       in 500..511 -> {
                           error.value = TypeError.SERVICE
                       }
                        in 400..451 -> {
                           error.value = TypeError.NETWORK
                        }else -> {
                            error.value = TypeError.OTHER
                        }
                    }
                }
            }

    }

    suspend fun getDirectionsMap(url: String, origin:String, destination:String, key:String): Response<DirectionResponses> {
        return mapasRepository.getDirections(url, origin,destination, key)
    }

}