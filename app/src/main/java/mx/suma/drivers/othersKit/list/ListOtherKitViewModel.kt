package mx.suma.drivers.othersKit.list

import androidx.lifecycle.*
import kotlinx.coroutines.*
import mx.suma.drivers.session.AppState
import mx.suma.drivers.utils.getCurrentHour
import mx.suma.drivers.utils.now

class ListOtherKitViewModel(
    val appState: AppState
) : ViewModel() {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val navigateToKitLimpieza = MutableLiveData(false)
    val navigateToADBLUE = MutableLiveData(false)
    val navigateToSparePart = MutableLiveData(false)

    init {

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onNavigationComplete() {
        navigateToKitLimpieza.value = false
    }

    fun onNavigateToKitLimpieza() {
        navigateToKitLimpieza.value = true
    }

    fun onNavigateToADBLUE() {
        navigateToADBLUE.value = true
    }

    fun onNavigateToSparePart() {
        navigateToSparePart.value = true
    }

    fun esActivoEnTiempo():Boolean {
        val currentHour = now().getCurrentHour()
        return (currentHour in 9..15)
    }
}