package mx.suma.drivers.mantenimientos.listado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.repositories.mantenimientos.MantenimientosRepository

class ListadoMantenimientosViewModelFactory(val repository: MantenimientosRepository) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListadoMantenimientosViewModel::class.java)) {
            return ListadoMantenimientosViewModel(
                repository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}