package mx.suma.drivers.othersKit.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.session.AppState

class CaptureOtherKitViewModelFactory(
    val appState: AppState
): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaptureOtherKitViewModel::class.java)) {
            return CaptureOtherKitViewModel(
                appState
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}