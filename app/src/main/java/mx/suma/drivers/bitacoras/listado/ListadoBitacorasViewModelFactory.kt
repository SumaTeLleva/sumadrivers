package mx.suma.drivers.bitacoras.listado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.repositories.bitacoras.BitacorasRepository
import mx.suma.drivers.repositories.operadores.OperadoresRepository

class ListadoBitacorasViewModelFactory(
    private val bitacorasRepository: BitacorasRepository,
    private val operadoresRepository: OperadoresRepository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListadoBitacorasViewModel::class.java)) {
            return ListadoBitacorasViewModel(
                bitacorasRepository,
                operadoresRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}