package mx.suma.drivers.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.database.UsuarioDao
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.repositories.archivos.ArchivosRepository
import mx.suma.drivers.repositories.dispositivos.DispositivosRepository
import mx.suma.drivers.session.AppState

class PanelViewModelFactory(
    val appState: AppState,
    val usuarioDao: UsuarioDao,
    val dispositivosRepository: DispositivosRepository,
    private val service: ApiSuma,
    val archivos: ArchivosRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PanelViewModel::class.java)) {
            return PanelViewModel(appState, usuarioDao, dispositivosRepository, service, archivos) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}