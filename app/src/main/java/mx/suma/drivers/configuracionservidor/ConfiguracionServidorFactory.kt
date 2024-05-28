package mx.suma.drivers.configuracionservidor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.session.AppState

class ConfiguracionServidorFactory(
    val appState: AppState,
    val path: String
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfiguracionServidorViewModel::class.java)) {
            return ConfiguracionServidorViewModel(
                appState,
                path
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}