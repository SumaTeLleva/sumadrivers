package mx.suma.drivers.tickets.captura

import androidx.lifecycle.*
import androidx.lifecycle.Transformations.map
import kotlinx.coroutines.*
import mx.suma.drivers.models.api.utils.asProveedorDbModel
import mx.suma.drivers.models.db.ProveedorModel
import mx.suma.drivers.models.db.TicketModel
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.models.db.getPayload
import mx.suma.drivers.models.db.validators.TicketValidator
import mx.suma.drivers.models.utils.Kilometraje
import mx.suma.drivers.repositories.proveedores.ProveedoresRepository
import mx.suma.drivers.repositories.tickets.TicketsRepository
import mx.suma.drivers.repositories.unidades.UnidadesRepository
import mx.suma.drivers.session.AppState
import mx.suma.drivers.utils.formatAsDateTimeString
import mx.suma.drivers.utils.now
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.net.UnknownHostException
import java.util.*

class CapturaTicketViewModel(
    val appState: AppState,
    private val proveedoresRepository: ProveedoresRepository,
    private val ticketsRepository: TicketsRepository,
    private val unidadesRepository: UnidadesRepository
) : ViewModel() {

    val usuario = MutableLiveData<UsuarioModel>()

    private val currentTime = MutableLiveData<Calendar>()
    val currentTimeString = map(currentTime) {
        it.time.formatAsDateTimeString()
    }

    val _gasolineras = MutableLiveData<List<ProveedorModel>>()
    val gasolineras = map(_gasolineras) {
        it.map {proveedor -> "${proveedor.id} - ${proveedor.nombre}" }
    }

    private val ticket = TicketModel()

    val data = MutableLiveData<TicketModel>()

    val ticketValido = map(data) {
        TicketValidator.isValid(it)
    }

    val ticketGenerado = MutableLiveData(-1L)

    val monto = MutableLiveData("$0.0")

    val kilometraje = MutableLiveData<Kilometraje>(Kilometraje(0,0f))

    private val _isSendingRequest = MutableLiveData(false)
    val sendingRequest: LiveData<Boolean>
        get() = _isSendingRequest

    private val _failedRequest = MutableLiveData(false)
    val failedRequest: LiveData<Boolean>
        get() = _failedRequest

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String>
        get() = _errorMessage

    val hasErrorMessage = map(errorMessage) {
        it.isNotEmpty()
    }

    private val _controlsEnabled = MediatorLiveData<Boolean>()
    val controlsEnabled: LiveData<Boolean>
        get() = _controlsEnabled

    init {
        currentTime.value = now()
        data.value = ticket
        bajarGasolineras()

        _controlsEnabled.addSource(ticketValido) {
            _controlsEnabled.value = combineLatest(_isSendingRequest, ticketValido)
        }

        _controlsEnabled.addSource(_isSendingRequest) {
            _controlsEnabled.value = combineLatest(_isSendingRequest, ticketValido)
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

    private fun bajarGasolineras() {
        viewModelScope.launch {
            _isSendingRequest.value = true
            try {
                val result = proveedoresRepository.obtenerGasolineras().asProveedorDbModel()
                _gasolineras.value = result
            }catch (e: UnknownHostException) {
                _errorMessage.value = "Sin conexion a internet"
                _failedRequest.value = true
            }catch (e:Exception) {
                _errorMessage.value = "Ocurrio un problema al obtener las gasolineras"
                _failedRequest.value = true
            }finally {
                _isSendingRequest.value = false
            }
        }
    }

    fun setKilometraje(value: String) {
        ticket.kilometraje = value.toLong()
        data.value = ticket
    }

    fun setLitros(value: String) {
        ticket.litros = value.toDouble()
        data.value = ticket

        updateMonto()
    }

    fun setPrecio(value: String) {
        ticket.precioCombustible = value.toDouble()
        data.value = ticket

        updateMonto()
    }

    fun setFolio(value: String) {
        ticket.folio = value
        data.value = ticket
    }

    fun getGasolinera(): Long = ticket.idGasolinera

    fun setGasolinera(item: String) {
        val id = item.split("-")[0].replace(" ", "").toLong()
        ticket.idGasolinera = id

        Timber.i("Id seleccionado: $id")
    }

    private suspend fun guardarUltimoKilometraje(unidad: Long, value: Long) {
        try {
            unidadesRepository.guardarNuevoKilometraje(unidad, value)
        }catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun onGuardarTicket() {
        val result = TicketValidator.isValid(ticket)
        println("Guardar ticket: $result")
        if(result) {
            _isSendingRequest.value = true

            val location = appState.getLastKnownLocation()

            usuario.value?.let {
                ticket.apply {
                    idOperador = it.idOperadorSuma
                    idUnidad = it.idUnidad
                    monto = ticket.litros * ticket.precioCombustible
                    fecha = currentTimeString.value as String
                    latitud = location["latitud"] as Double
                    longitud = location["longitud"] as Double
                }

                viewModelScope.launch {
                    try {
                        val response = ticketsRepository.guardarTicket(ticket.getPayload())
                        if(response.data.id != -1L) {
                            guardarUltimoKilometraje(ticket.idUnidad, ticket.kilometraje)
                            ticketGenerado.value = response.data.id
                            Timber.i("Ticket generado, regresar a listado")
                        }
                    } catch (a: HttpException) {
                        println(a)
                        val msg = withContext(Dispatchers.IO) {
                            a.response()?.let {
                                try {
                                    it.errorBody()?.let { errorBody ->
                                        val error = JSONObject(errorBody.string())

                                        Timber.i(error.getString("mensaje"))

                                        error.getString("mensaje")
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e)
                                    "No hay mensaje"
                                }
                            }
                        }

                        _errorMessage.value = msg
                        _failedRequest.value = true
                    } catch (e: Exception) {
                        Timber.d(e)
                    }

                    _isSendingRequest.value = false
                }
            }
        }
    }

    fun convertCurrencyToDouble (it:String):Double {
        var formatted: Double = 0.0
        try {
            val cleanString: String = it.replace("""[$,.]""".toRegex(), "")
            val parsed = cleanString.toDouble()
            formatted = (parsed / 100)

        }catch (e: Exception) {
            println(e)
        }
        return formatted
    }

    fun onNavigationComplete() {
        ticketGenerado.value = -1L
    }

    private fun updateMonto() {
        if(ticket.litros > 0 && ticket.precioCombustible > 0.0) {
            monto.value = "\$${ticket.litros * ticket.precioCombustible}"
        }else {
            monto.value = "\$${0.0}"
        }
    }

    suspend fun getUltimoKilometraje(id: Long): Kilometraje {
        Timber.d("getUltimoKilometraje")
        var resultado = Kilometraje()
        try {
            val response = unidadesRepository.bajarUnidad(id)
            resultado = Kilometraje(response.attr.kilometraje)
        } catch (a: HttpException) {
            Timber.d(a)
        }

        return resultado
    }

}
