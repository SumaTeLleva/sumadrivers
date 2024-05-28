package mx.suma.drivers.appstarter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.session.AppState

class StarterAppViewModelFactory(private val appState: AppState) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(StarterAppViewModel::class.java)) {
            return StarterAppViewModel(appState) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}