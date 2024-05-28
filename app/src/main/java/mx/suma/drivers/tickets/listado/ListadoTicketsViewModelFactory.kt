package mx.suma.drivers.tickets.listado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.repositories.tickets.TicketsRepository

class ListadoTicketsViewModelFactory(val repository: TicketsRepository) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ListadoTicketsViewModel::class.java)) {
            return ListadoTicketsViewModel(
                repository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}