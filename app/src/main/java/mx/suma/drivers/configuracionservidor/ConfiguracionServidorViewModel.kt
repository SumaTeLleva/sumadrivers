package mx.suma.drivers.configuracionservidor

import androidx.lifecycle.ViewModel
import mx.suma.drivers.session.AppState

class ConfiguracionServidorViewModel(
    val appState: AppState,
    val path: String
) : ViewModel() {
    var host_server: String = path

    fun guardarHostServer(host: String) {
        appState.setPathServer(host)
    }
}