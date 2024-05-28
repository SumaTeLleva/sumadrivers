package mx.suma.drivers.bitacoras.captura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.repositories.bitacoras.BitacorasRepository
import mx.suma.drivers.repositories.scanner.ScannerRepository
import mx.suma.drivers.repositories.unidades.UnidadesRepository
import mx.suma.drivers.session.AppState
import mx.suma.drivers.utils.ConnectivityObserver

class CapturaBitacoraViewModelFactory(
    val appState: AppState,
    val connectivityObserver: ConnectivityObserver,
    val repository: BitacorasRepository,
    val repositoryUnit: UnidadesRepository,
    val repositoryScanner: ScannerRepository,
    val id: Long,
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CapturaBitacoraViewModel::class.java)) {
            return CapturaBitacoraViewModel(appState, connectivityObserver, repository, repositoryUnit,repositoryScanner, id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}