package mx.suma.drivers.media.audio

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecordAudioViewModel : ViewModel() {

    val navigateToCapturaSanitizacion = MutableLiveData<Boolean>(false)

    fun onNavigateToCapturaSanitizacion() {
        navigateToCapturaSanitizacion.value = true
    }

    fun onNavigationComplete() {
        navigateToCapturaSanitizacion.value = false
    }
}