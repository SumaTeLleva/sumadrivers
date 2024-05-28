package mx.suma.drivers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.database.UsuarioDao

class SumaDriversViewModelFactory(val usuarioDao: UsuarioDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SumaDriversViewModel::class.java)) {
            return SumaDriversViewModel(usuarioDao) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}