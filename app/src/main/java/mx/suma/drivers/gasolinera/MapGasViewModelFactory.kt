package mx.suma.drivers.gasolinera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.repositories.mapas.MapasRepository
import mx.suma.drivers.repositories.proveedores.ProveedoresRepository

class MapGasViewModelFactory(
    val proveedorRepository: ProveedoresRepository,
    val mapasRepository: MapasRepository
): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapGasViewModel::class.java)) {
            return MapGasViewModel(proveedorRepository, mapasRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}