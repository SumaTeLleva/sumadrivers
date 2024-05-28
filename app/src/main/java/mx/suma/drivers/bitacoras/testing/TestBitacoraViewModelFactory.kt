package mx.suma.drivers.bitacoras.testing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.repositories.archivos.ArchivosRepository
import mx.suma.drivers.repositories.encuestas.EncuestasRepository
import mx.suma.drivers.repositories.unidades.UnidadesRepository
import mx.suma.drivers.session.AppState

class TestBitacoraViewModelFactory(
    val appState: AppState,
    val encuestaRepository: EncuestasRepository,
    val unidadesRepository: UnidadesRepository,
    val archivoRepository: ArchivosRepository,
    val unidadId: Int,
    val operadorId: Int,
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestBitacoraViewModel::class.java)) {
            return TestBitacoraViewModel(
                appState,
                encuestaRepository,
                unidadesRepository,
                archivoRepository,
                unidadId,
                operadorId,
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

}