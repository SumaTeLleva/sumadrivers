package mx.suma.drivers.sanitizaciones.captura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.repositories.bitacoras.BitacorasRepository
import mx.suma.drivers.repositories.clientes.ClientesRepository
import mx.suma.drivers.repositories.sanitizaciones.SanitizacionesRepository

class CapturaSanitizacionViewModelFactory(val bitacorasRepository: BitacorasRepository, val clientesRepository: ClientesRepository, val sanitizacionesRepository: SanitizacionesRepository) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CapturaSanitizacionViewModel::class.java)) {
            return CapturaSanitizacionViewModel(bitacorasRepository, clientesRepository, sanitizacionesRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}