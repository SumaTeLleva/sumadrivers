package mx.suma.drivers.directorio

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import mx.suma.drivers.models.api.utils.asContactoDbModel
import mx.suma.drivers.models.db.ContactoModel
import mx.suma.drivers.repositories.directorio.DirectorioTelefonicoRepository
import mx.suma.drivers.utils.EstatusLCE
import mx.suma.drivers.utils.TypeError
import timber.log.Timber
import java.net.UnknownHostException

class DirectorioViewModel(
    val repository: DirectorioTelefonicoRepository,
) : ViewModel() {
    val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    var directorioMemory = MutableLiveData<List<ContactoModel>>()
    var directorio = MutableLiveData<ArrayList<ContactoModel>>()
    val error = MutableLiveData(TypeError.EMPTY)
    val estatus = MutableLiveData(EstatusLCE.LOADING)

    val hacerLlamada = MutableLiveData(false)
    val phoneNumber = MutableLiveData("")

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun getDirectorio() {
        uiScope.launch {
            estatus.value = EstatusLCE.LOADING
            try {
                val result = repository.obtenerDirectorio().asContactoDbModel()
                if(result.isNotEmpty()){
                    directorio.value = ArrayList<ContactoModel>(result);
                    directorioMemory.value = result
                    estatus.value = EstatusLCE.CONTENT
                }else {
                    estatus.value = EstatusLCE.NO_CONTENT
                }
            }catch (e: UnknownHostException) {
                estatus.value = EstatusLCE.ERROR
                error.value = TypeError.NETWORK
            }catch (e: Exception) {
                estatus.value = EstatusLCE.ERROR
                error.value = TypeError.SERVICE
                println(e);
            }
        }
    }

    fun filterDirectory(coincidence: String?) {
        if(coincidence.isNullOrEmpty()) {
            directorio.value = ArrayList<ContactoModel>(directorioMemory.value);
        } else {
            var collecion: List<ContactoModel>? = directorio.value?.filter { it -> it.nombre.lowercase().contains(coincidence.lowercase()) }
            directorio.value = ArrayList<ContactoModel>(collecion)
        }
    }

    fun onContactoClicked(telefono: String) {
        Timber.d("Teléfono: $telefono")
        hacerLlamada.value = true
        phoneNumber.value = telefono
    }

    fun onLlamadaRealizada() {
        hacerLlamada.value = false
        phoneNumber.value = ""
    }
}
