package mx.suma.drivers.miUnidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.database.UsuarioDao
import mx.suma.drivers.repositories.unidades.UnidadesRepository
import mx.suma.drivers.session.AppState

class MiUnidadViewModelFactory(val unidadesRepository: UnidadesRepository, val usuarioDao: UsuarioDao, val appState: AppState) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MiUnidadViewModel::class.java)) {
            return MiUnidadViewModel(unidadesRepository, usuarioDao, appState) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}