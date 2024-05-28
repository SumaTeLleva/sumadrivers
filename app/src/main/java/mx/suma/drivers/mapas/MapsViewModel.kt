package mx.suma.drivers.mapas

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mx.suma.drivers.models.api.Mapa
import mx.suma.drivers.models.api.utils.DirectionResponses
import mx.suma.drivers.repositories.mapas.MapasRepository
import retrofit2.Response
import timber.log.Timber
import java.lang.Exception

class MapsViewModel(val mapasRepository: MapasRepository) : ViewModel() {
    val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val mapa = MutableLiveData<Mapa>(Mapa())
    val destino = MutableLiveData<LatLng>(null)
    val runMap = MutableLiveData<Boolean>(false)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun downloadMap(id: Long) {
        uiScope.launch {
            try {
                val result = mapasRepository.getMapa(id)
                mapa.value = result
                runMap.value = true
                Timber.i("Got a map!")
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun downloadDestination(id: Long, callback: ()-> Unit) {
        uiScope.launch{
            try {
                val result = mapasRepository.getDestination(id)
                if(result.data.isNotEmpty()) {
                    result.data[0].let {
                        destino.value = LatLng(it.LATITUD.toDouble(), it.LONGITUD.toDouble())
                        runMap.value = true
                    }
                }
            }catch (e: Exception) {
                callback()
            }
        }
    }

    suspend fun getDirectionsMap(url: String, origin:String, destination:String, key:String): Response<DirectionResponses> {
        return mapasRepository.getDirections(url, origin,destination, key)
    }
}