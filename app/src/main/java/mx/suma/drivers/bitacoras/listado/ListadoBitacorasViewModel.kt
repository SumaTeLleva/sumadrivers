package mx.suma.drivers.bitacoras.listado

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import mx.suma.drivers.models.db.BitacoraModel
import mx.suma.drivers.models.api.Bitacora
import mx.suma.drivers.models.api.utils.asBitacoraDbModel
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.repositories.bitacoras.BitacorasRepository
import mx.suma.drivers.repositories.operadores.OperadoresRepository
import mx.suma.drivers.utils.*
import retrofit2.HttpException
import timber.log.Timber
import java.util.*

class ListadoBitacorasViewModel(
    private val bitacorasRepository: BitacorasRepository,
    private val operadoresRepository: OperadoresRepository
) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val usuario = MutableLiveData<UsuarioModel>()
    val errorVisit = MutableLiveData("")

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    val estatus = MutableLiveData(EstatusLCE.LOADING)
    val error = MutableLiveData(TypeError.EMPTY)

    val bitacoras = MutableLiveData<List<BitacoraModel>>()

    val currentDay = MutableLiveData<Calendar>()

    val currentDate = Transformations.map(currentDay) {
        it.time.formatDateAsFriendlyFormat()
    }

    val onNavigateToCapturaBitacora = MutableLiveData(-1L)

    init {
        currentDay.value = Calendar.getInstance()
        bitacoras.value = arrayListOf()
    }

    fun getBitacoraDb() {
        uiScope.launch {
            val response = bitacorasRepository.getBitacorasDb()
            if(response.isNullOrEmpty()) {
                error.value = TypeError.NETWORK
            }else {
                bitacoras.value = response
                estatus.value = EstatusLCE.CONTENT

            }
        }
    }

    fun getBitacoras() {
        uiScope.launch {
            estatus.value = EstatusLCE.LOADING
            currentDay.value?.let {
                try {
                    val desde = it.startOfDay()
                    val hasta = it.endOfDay()
                    usuario.value?.let { usuario ->
                        val result = bajarBitacoras(usuario, desde.time, hasta.time)
                        if (result.isNotEmpty()) {
                            bitacoras.value = result.asBitacoraDbModel()
                            estatus.value = EstatusLCE.CONTENT
                        } else {
                            // TODO: Se debe mostrar un botón para reintentar la carga
                            estatus.value = EstatusLCE.NO_CONTENT
                        }
                        bitacorasRepository.guardarBitacorasDb(result)
                    }
                } catch (e: Exception) {
                    // TODO: Mostrar error y botón de acción
                    estatus.value = EstatusLCE.ERROR
                    error.value = TypeError.SERVICE
                }
            }
        }
    }

    private suspend fun bajarBitacoras(usuario: UsuarioModel, desde: Date, hasta: Date): List<Bitacora> {
        return bitacorasRepository.buscarBitacoras(usuario.idProveedor, usuario.idOperadorSuma, desde, hasta)
    }

    fun onToday() {
        currentDay.value = now()

        getBitacoras()
    }

    fun onNext24h() {
        currentDay.value?.let {
            val current = it.startOfDay()
            current.add(Calendar.HOUR_OF_DAY, 24)
            currentDay.value = current
            getBitacoras()
        }
    }

    fun onPrevious24h() {
        currentDay.value?.let {
            val current = it.startOfDay()
            current.add(Calendar.HOUR_OF_DAY, -24)

            currentDay.value = current

            getBitacoras()
        }
    }

    fun onBitacoraClicked(id: Long) {
        Timber.d("Id: $id")

        onNavigateToCapturaBitacora.value = id
    }

    fun onNavigationComplete() {
        onNavigateToCapturaBitacora.value = -1L
    }

    fun onGenerarVisitaOficina() {
        uiScope.launch {
            try {
                usuario.value?.let { usuario ->
                    operadoresRepository.generarVisitaOficina(usuario.idOperadorSuma)
                    onToday()
                }

            } catch (e: HttpException) {
                if(e.code() == 500){
                    errorVisit.value = "Ocurrio un problema en el servicio"
                }else {
                    errorVisit.value = e.response()?.message()
                }
            }
        }
    }

    fun onGenerarVisitaTaller() {
        uiScope.launch {
            try {
                usuario.value?.let { usuario ->
                    operadoresRepository.generarVisitaTaller(usuario.idOperadorSuma)
                    onToday()
                }
            } catch (e: HttpException) {
                if(e.code() == 500){
                    errorVisit.value = "Ocurrio un problema en el servicio"
                }else {
                    errorVisit.value = e.response()?.message()
                }
            }
        }
    }
}
