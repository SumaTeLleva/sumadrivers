package mx.suma.drivers.bitacoras.captura

import androidx.lifecycle.*
import kotlinx.coroutines.*
import mx.suma.drivers.models.api.utils.asDbModel
import mx.suma.drivers.models.db.BitacoraModel
import mx.suma.drivers.models.db.ScannerModel
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.models.db.getPayload
import mx.suma.drivers.models.utils.EstatusServicio
import mx.suma.drivers.models.utils.EstatusServicio.Estatus.*
import mx.suma.drivers.repositories.bitacoras.BitacorasRepository
import mx.suma.drivers.repositories.scanner.ScannerRepository
import mx.suma.drivers.repositories.unidades.UnidadesRepository
import mx.suma.drivers.session.AppState
import mx.suma.drivers.utils.*
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.lang.NullPointerException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class CapturaBitacoraViewModel(
    val appState: AppState,
    val connectivityObserver: ConnectivityObserver,
    val repository: BitacorasRepository,
    val repositoryUnit: UnidadesRepository,
    val repositoryScanner: ScannerRepository,
    val id: Long,
) : ViewModel() {

    private lateinit var bitacoraModel: BitacoraModel

    val usuario = MutableLiveData<UsuarioModel>()

    val data = MutableLiveData<BitacoraModel>()
    val isLoading = MutableLiveData(false)
    val fechaActual = MutableLiveData<Date>()
    val withInternet = MutableLiveData<ConnectivityObserver.Status>(ConnectivityObserver.Status.Unavailable)

    var ultimoKilometraje = MutableLiveData<Long>(0)
    var ultimoFolioBitacora: Long = appState.getUltimoFolioBitacora() + 1

    val fechaBitacora = Transformations.map(data) {
        parseDateTimeString(it.fecha).time.formatDateAsDateString()
    }

    val horariosBitacora = Transformations.map(data) {
        val inicial = parseDateTimeString(it.tiempoInicial).time
        val final = parseDateTimeString(it.tiempoFinal).time
        "De: ${inicial.formatDateAsTimeAmPmString()} a ${final.formatDateAsTimeAmPmString()}"
    }

    val estatusBitacora = Transformations.map(data) {
        when(EstatusServicio.getStatus(it)) {
            SERVICIO_NO_CONFIRMABLE -> "SERVICIO NO CONFIRMADO"
            SERVICIO_TERMINADO -> "SERVICIO TERMINADO (U${it.idUnidad})"
            CONFIRMAR_SERVICIO -> "SERVICIO POR CONFIRMAR"
            SERVICIO_PROGRAMADO -> "SERVICIO PROGRAMADO"
            ABRIR_BITACORA, INICIAR_RUTA -> "SERVICIO CONFIRMADO"
            else -> "SERVICIO EN RUTA (U${it.idUnidad})"
        }
    }


    val estatusAccion = Transformations.map(data) {
        when(EstatusServicio.getStatus(it)) {
            CONFIRMAR_SERVICIO -> "1. CONFIRMAR SERVICIO"
            ABRIR_BITACORA -> "2. ABRIR BITÁCORA"
            INICIAR_RUTA -> "3. INICIAR RUTA"
            TERMINAR_RUTA -> "4. TERMINAR RUTA"
            CERRAR_BITACORA -> "5. CERRAR BITÁCORA"
            SERVICIO_TERMINADO -> "SERVICIO TERMINADO"
            SERVICIO_PROGRAMADO -> "SERVICIO PROGRAMADO"
            SERVICIO_NO_CONFIRMABLE -> "SERVICIO NO CONFIRMADO"
            SERVICIO_TALLER -> "SERVICIO TALLER"
        }
    }

    val datosValidos = Transformations.map(data) {
        Transformations.map(withInternet) {
            statusConnect ->
            Timber.i("Validando datos ${data.value?.getPayload()}")
            if(statusConnect == ConnectivityObserver.Status.Available) {
                when(EstatusServicio.getStatus(it)) {
                    ABRIR_BITACORA -> {
                        it.folioBitacora > 0 && it.kilometrajeInicial > 0
                    }
                    TERMINAR_RUTA -> {
                        it.numeroPersonas > 0
                    }
                    CERRAR_BITACORA -> {
                        it.kilometrajeFinal > 0
                    }
//            CONFIRMAR_SERVICIO -> {
//                it.idRuta != 1400L
//            }
                    else -> true
                }
            }else {
                false
            }
        }
    }

    fun observerNetwork() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { value ->
                withInternet.value = value
            }
        }
    }


    val estatus = Transformations.map(data) {
        EstatusServicio.getStatus(it)
    }

    val mostrarConfirmacion = Transformations.map(estatus) {
        it == CONFIRMAR_SERVICIO
    }

    val mostrarAbrirBitacora = Transformations.map(estatus) {
        it == ABRIR_BITACORA
    }

    val mostrarIniciarRuta = Transformations.map(estatus) {
        it == INICIAR_RUTA
    }

    val mostrarTerminarRuta = Transformations.map(estatus) {
        it == TERMINAR_RUTA
    }

    val mostrarCerrarBitacora = Transformations.map(estatus) {
        it == CERRAR_BITACORA
    }

    val mostrarNoCerradoBitacora = Transformations.map(estatus) {
        it == SERVICIO_NO_CONFIRMABLE
    }

    private val _isSendingRequest = MutableLiveData(false)
    val isSendingRequest: LiveData<Boolean>
        get() = _isSendingRequest


    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String>
        get() = _errorMessage

    val mostrarLetrero = MutableLiveData(false)
    val iniciarCapturaAforo = MutableLiveData(false)
    val mostrarEscaner = MutableLiveData(false)

    val hasErrorMessage = Transformations.map(errorMessage) {
        it.isNotEmpty()
    }

    val _navigateToMapa = MutableLiveData(false)

//    init {
////        buscarBitacora()
//        viewModelScope.launch {
//
//        }
//    }

    suspend fun obtenerFechaActual(format:String? = "yyyy-MM-d'T'HH:mm:ss"):Date {
        var dateCurrent:Date = Date()
        try {
            val result = repository.obtenerFechaActual().asDbModel()
             dateCurrent = SimpleDateFormat(format, Locale.ROOT).parse(result.data.FECHA_HORA)!!

        }catch (e: Exception) {
            Timber.e(e)
        }
        return dateCurrent!!
    }

    fun onConfirmarServicio() {
        usuario.value?.let {
            bitacoraModel.idUnidad = it.idUnidad
//            bitacoraModel.horaConfirmacion = now().time.formatAsDateTimeString()
            enviarCambios()
        }
    }

    fun onAbrirBitacora() {
//        bitacoraModel.horaBanderazo = now().time.formatAsDateTimeString()
        enviarCambios()

        onActivarLetrero()
        onActivarLectorAforo()

        // TODO: Validar que hayan llegado los datos al servidor antes de setear esto
        appState.setUltimoFolioBitacora(bitacoraModel.folioBitacora)
    }

    fun onIniciarServicio() {
//        bitacoraModel.horaInicioRuta = now().time.formatAsDateTimeString()
        enviarCambios()

        onActivarLetrero()
        onActivarLectorAforo()
    }

    fun onTerminarServicio() {
//        bitacoraModel.horaFinalRuta = now().time.formatAsDateTimeString()

        enviarCambios()
    }

    fun onCerrarBitacora() {
//        bitacoraModel.horaCierreRuta = now().time.formatAsDateTimeString()
        enviarCambios()
    }

    fun setFolioBitacora(folio: Long) {
        if(::bitacoraModel.isInitialized) {
            bitacoraModel.folioBitacora = folio
            data.value = bitacoraModel
        }
    }

    fun setKilometrajeInicial(kmInicial: Long) {
        if(::bitacoraModel.isInitialized){
            bitacoraModel.kilometrajeInicial = kmInicial
            data.value = bitacoraModel
        }

        appState.setUltimoKilometrajeInicial(kmInicial)
    }

    fun setNumeroPersonas(personas: Int) {
        if(this::bitacoraModel.isInitialized) {
            bitacoraModel.numeroPersonas = personas
            data.value = bitacoraModel
        }
    }

    fun setKilometrajeFinal(kmFinal: Long) {
        if(this::bitacoraModel.isInitialized) {
            bitacoraModel.kilometrajeFinal = kmFinal
            data.value = bitacoraModel
        }

        appState.setUltimoKilometraje(kmFinal)
    }

    private suspend fun guardarUltimoKilometraje(unidad: Long, value: Long) {
        try {
            repositoryUnit.guardarNuevoKilometraje(unidad, value)
        }catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun buscarBitacora(isLocal:Boolean) {
        _isSendingRequest.value = true
        viewModelScope.launch {
            try {
                if(!isLocal) {
                    bitacoraModel = repository.buscarBitacora(id).asDbModel()
                    guardarRegistroDB(bitacoraModel)
                }else {
                    bitacoraModel = obtenerRegistroDB(id)
                }
                if(EstatusServicio.getStatus(bitacoraModel) == ABRIR_BITACORA) {
                    val unidad = repositoryUnit.bajarUnidad(bitacoraModel.idUnidad)
                    appState.setUltimoKilometraje(unidad.attr.kilometraje)
                    ultimoKilometraje.value = unidad.attr.kilometraje
                    bitacoraModel.folioBitacora = ultimoFolioBitacora
                    bitacoraModel.kilometrajeInicial = unidad.attr.kilometraje
                }
                data.value = bitacoraModel
            } catch (e: Exception) {
                Timber.i("Error en algo, que hacemos?")
                Timber.d(e)
            } finally {
                _isSendingRequest.value = false
            }
        }
    }


    fun mostrarTesting(): Boolean {
        var response: Boolean
        val lastTest = appState.getLastTesting()
        if(lastTest["lastUnit"].toString().toInt() == data.value?.idUnidad?.toInt()) {
            fechaActual.value.let {
                try {
                    response = Date(it?.time!!).after(Date(lastTest["lastDate"].toString().toLong()))
                }catch (e:NullPointerException) {
                    response = false
                }
            }
        } else {
            response = true
        }

        return response
    }

     fun enviarCambios() {
        _isSendingRequest.value = true
        isLoading.value = true
        viewModelScope.launch {
            try {
                val currentDate = obtenerFechaActual()
                bitacoraModel = EstatusServicio.updateTimeByStatus(estatus, bitacoraModel, currentDate.formatAsDateTimeString())
                bitacoraModel = repository.guardarBitacora(id, bitacoraModel.getPayload()).asDbModel()
                val futureStatus = EstatusServicio.getStatus(bitacoraModel)
                if(futureStatus == ABRIR_BITACORA) {
                    bitacoraModel.folioBitacora = ultimoFolioBitacora
                    bitacoraModel.kilometrajeInicial = ultimoKilometraje.value!!
                }
                if(estatus.value == CERRAR_BITACORA) {
                    guardarUltimoKilometraje(bitacoraModel.idUnidad, bitacoraModel.kilometrajeFinal)
                }
                data.value = bitacoraModel
                guardarRegistroDB(bitacoraModel)
                _errorMessage.value = ""
            } catch (a: HttpException) {
                val msg = withContext(Dispatchers.IO) {
                    a.response()?.let {
                        try {
                            it.errorBody()?.let { errorBody ->
                                val error = JSONObject(errorBody.string())
                                Timber.i(error.getString("mensaje"))
                                error.getString("mensaje")
                            }
                        } catch (e: java.lang.Exception) {
                            Timber.e(e)
                            "No hay mensaje"
                        }
                    }
                }

                _errorMessage.value = msg
            }catch (e:UnknownHostException) {
                _errorMessage.value = "Sin conexion a internet"
            } catch (e:Exception) {
                _errorMessage.value = "Ocurrio un problema en la aplicación"
            }
            finally {
                    _isSendingRequest.value = false
                    isLoading.value = false
            }
        }
    }

    fun onNavigateToMapa() {
        _navigateToMapa.value = true
    }

    fun onNavigationComplete() {
        _navigateToMapa.value = false
    }

    fun onActivarLetrero() {
        mostrarLetrero.value = true
    }

    fun onActivarLetreroDone() {
        mostrarLetrero.value = false
    }

    fun onMostrarEscaner() {
        mostrarEscaner.value = true
    }

    fun onMostrarEscanerDone() {
        mostrarEscaner.value = false
    }

    fun onActivarLectorAforo() {
        iniciarCapturaAforo.value = true
    }

    fun onActivarLectorAforoDone() {
        iniciarCapturaAforo.value = false
    }

    fun guardarRegistroDB(bitacora: BitacoraModel) {
        viewModelScope.launch {
            val dbresult = obtenerRegistroDB(bitacora.id)
            if(dbresult != null) {
                repository.actualizarBitacoraDb(bitacora)
            }else {
                repository.guardarBitacoraDb(bitacora)
            }
        }
    }
    suspend fun obtenerRegistroDB(id: Long):BitacoraModel {
        return repository.buscarBitacoraByIdDb(id)
    }

    fun obtenerScannerDbEnvio(onCallback:() -> Unit) {
        viewModelScope.launch {
            try {
                val listScanner:List<ScannerModel> = repositoryScanner.obtenerScannerByBitacoraDb(id)
                coroutineScope {
                    listScanner.map {
                        async {
                            guardarRegistroScanner(it) { itOk, message ->
                                viewModelScope.launch {
                                    if(itOk){repositoryScanner.eliminarScannerDb(it)}
                                    Timber.d(message)
                                }
                            }
                        }
                    }.awaitAll()
                    if(listScanner.size > 0) {
                        onCallback()
                    }
                }
            }catch (e:Exception) {
                Timber.e(e)
            }
        }
    }

    fun guardarRegistroScannerDb(scannerData: ScannerModel, onCallback:(Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val searchResult = repositoryScanner.verifyDataScannerDb(
                    scannerData.bitacoraId,
                    scannerData.clienteId,
                    scannerData.pasajeroId
                )
                if(searchResult == null) {
                    repositoryScanner.guardarScannerDb(scannerData)
                    onCallback(true, "Cuando regrese la conexón, se enviará la asistencia.")
                }else {
                    onCallback(false, "Asistencia existente en la memoria")
                }
            } catch (e:Exception) {
                Timber.e(e)
                onCallback(false, "Ocurrio un problema, favor de reportarlo")
            }
        }
    }

    fun guardarRegistroScanner(scannerData: ScannerModel, onCallback:(Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repositoryScanner.guardarScanner(scannerData.getPayload())
                onCallback(true, "Se envio la asistencia exitosamente")
            }catch (e:Exception) {
                Timber.e(e)
                onCallback(false, "Ocurrio un problema, favor de reportarlo")
            }
        }
    }
}

