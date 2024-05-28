package mx.suma.drivers.mapas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.repositories.mapas.MapasRepository

class MapsViewModelFactory(val mapasRepository: MapasRepository) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            return MapsViewModel(mapasRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}