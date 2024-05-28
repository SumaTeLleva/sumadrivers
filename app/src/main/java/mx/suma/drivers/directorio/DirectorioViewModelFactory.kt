package mx.suma.drivers.directorio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.repositories.directorio.DirectorioTelefonicoRepository

class DirectorioViewModelFactory(
    private val directorioTelefonicoRepository: DirectorioTelefonicoRepository
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(DirectorioViewModel::class.java)) {
            return DirectorioViewModel(directorioTelefonicoRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}