package mx.suma.drivers.tickets.captura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.repositories.proveedores.ProveedoresRepository
import mx.suma.drivers.repositories.tickets.TicketsRepository
import mx.suma.drivers.repositories.unidades.UnidadesRepository
import mx.suma.drivers.session.AppState

class CapturaTicketViewModelFactory(
    val appState: AppState,
    val proveedoresRepository: ProveedoresRepository,
    val ticketsRepository: TicketsRepository,
    val unidadesRepository: UnidadesRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CapturaTicketViewModel::class.java)) {
            return CapturaTicketViewModel(
                appState,
                proveedoresRepository,
                ticketsRepository,
                unidadesRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}