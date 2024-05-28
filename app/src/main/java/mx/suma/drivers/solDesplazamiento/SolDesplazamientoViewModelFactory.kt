package mx.suma.drivers.solDesplazamiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.session.AppState

class SolDesplazamientoViewModelFactory(
    val appState: AppState
): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SolDesplazamientoViewModel::class.java)) {
            return SolDesplazamientoViewModel(
                appState
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}