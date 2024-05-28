package mx.suma.drivers.carousel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.session.AppState

class CarouselViewModelFactory(
    val appState: AppState,
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarouselViewModel::class.java)) {
            return CarouselViewModel(
                appState
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}