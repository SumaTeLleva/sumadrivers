package mx.suma.drivers.solDesplazamiento

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import mx.suma.drivers.session.AppState

class SolDesplazamientoViewModel(
    val appState: AppState
): ViewModel() {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val navigateToRequestID = MutableLiveData(false)
    val navigateToRequestTH = MutableLiveData(false)
    val navigateToRequestAdmin = MutableLiveData(false)
    val navigateToRequestMante = MutableLiveData(false)
    val navigateToRequestStore = MutableLiveData(false)

    init {

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onNavigationComplete() {
        navigateToRequestID.value = false
        navigateToRequestTH.value = false
        navigateToRequestAdmin.value = false
        navigateToRequestMante.value = false
        navigateToRequestStore.value = false
    }

    fun onNavigateToRequestId() {
        navigateToRequestID.value = true
    }

    fun onNavigateToRequestTH() {
        navigateToRequestTH.value = true
    }

    fun onNavigateToRequestAdmin() {
        navigateToRequestAdmin.value = true
    }

    fun onNavigateToRequestMante() {
        navigateToRequestMante.value = true
    }

    fun onNavigateToRequestStore() {
        navigateToRequestStore.value = true
    }
}