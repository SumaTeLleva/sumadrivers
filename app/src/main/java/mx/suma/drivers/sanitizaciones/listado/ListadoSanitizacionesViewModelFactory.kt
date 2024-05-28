package mx.suma.drivers.sanitizaciones.listado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.repositories.sanitizaciones.SanitizacionesRepository

class ListadoSanitizacionesViewModelFactory(val sanitizacionesRepository: SanitizacionesRepository) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListadoSanitizacionesViewModel::class.java)) {
            return ListadoSanitizacionesViewModel(sanitizacionesRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}