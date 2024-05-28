package mx.suma.drivers.mantenimientos.captura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.repositories.mantenimientos.MantenimientosRepository

class CapturaMantenimientoViewModelFactory(val usuarioModel: UsuarioModel, val mantenimientosRepository: MantenimientosRepository) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CapturaMantenimientoViewModel::class.java)) {
            return CapturaMantenimientoViewModel(usuarioModel, mantenimientosRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}