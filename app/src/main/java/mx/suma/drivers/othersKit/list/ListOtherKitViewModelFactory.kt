package mx.suma.drivers.othersKit.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.session.AppState

class ListOtherKitViewModelFactory(
    val appState: AppState
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListOtherKitViewModel::class.java)) {
            return ListOtherKitViewModel(
                appState
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}