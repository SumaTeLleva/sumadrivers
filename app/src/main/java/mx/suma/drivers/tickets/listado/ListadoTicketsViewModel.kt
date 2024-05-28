package mx.suma.drivers.tickets.listado

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.suma.drivers.models.api.Ticket
import mx.suma.drivers.models.api.utils.asTicketDbModel
import mx.suma.drivers.models.db.TicketModel
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.repositories.tickets.TicketsRepository
import mx.suma.drivers.utils.*
import java.util.*

class ListadoTicketsViewModel(val repository: TicketsRepository) : ViewModel() {

    val estatus = MutableLiveData(EstatusLCE.LOADING)
    val error = MutableLiveData(TypeError.EMPTY)

    val usuario = MutableLiveData<UsuarioModel>()

    val tickets = MutableLiveData<List<TicketModel>>()

    val currentTime = MutableLiveData<Calendar>()

    val currentMonth = Transformations.map(currentTime) {
        it.time.formatDateAsMonthYearString()
    }

    val total = Transformations.map(tickets) {
        "Total de tickets: ${it.size}"
    }

    val navigateToCapturaTicketsFragment = MutableLiveData(false)
    val navigateToMapaGasActivity = MutableLiveData(false)

    init {
        tickets.value = arrayListOf()
        currentTime.value = now()
    }

    fun getTickets() {
        viewModelScope.launch {
            estatus.value = EstatusLCE.LOADING

            currentTime.value?.let {
                try {
                    val desde = it.startOfMonth().time
                    val hasta = it.endOfMonth().time

                    usuario.value?.let { usuario ->
                        val result = bajarTickets(usuario.idOperadorSuma, desde, hasta)

                        if (result.isNotEmpty()) {
                            tickets.value = result.asTicketDbModel()
                            estatus.value = EstatusLCE.CONTENT
                        } else {
                            estatus.value = EstatusLCE.NO_CONTENT
                        }
                    }
                } catch (e: Exception) {
                    estatus.value = EstatusLCE.ERROR
                    error.value = TypeError.SERVICE
                }
            }
        }
    }

    private suspend fun bajarTickets(idOperador: Long, desde: Date, hasta: Date): List<Ticket> {
        return repository.buscarTicketsPorRangoDeFechas(
            idOperador,
            desde.formatAsDateTimeString(), hasta.formatAsDateTimeString()
        )
    }

    fun onNavigateToCapturaTickets() {
        navigateToCapturaTicketsFragment.value = true
    }

    fun onNavigateToMapaGasolineras() {
        navigateToMapaGasActivity.value = true
    }

    fun onNavigationComplete() {
        navigateToCapturaTicketsFragment.value = false
        navigateToMapaGasActivity.value = false
    }

    fun onNextMonth() {
        currentTime.value = currentTime.value?.nextMonth()

        getTickets()
    }

    fun onPreviuosMonth() {
        currentTime.value = currentTime.value?.previousMonth()
        getTickets()
    }
}
