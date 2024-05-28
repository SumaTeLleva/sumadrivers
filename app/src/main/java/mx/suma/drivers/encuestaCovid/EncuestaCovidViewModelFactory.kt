package mx.suma.drivers.encuestaCovid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.database.UsuarioDao
import mx.suma.drivers.repositories.encuestas.EncuestasRepository
import mx.suma.drivers.session.AppState


class EncuestaCovidViewModelFactory(
    val encuestaId: Long,
    val encuestasRepository: EncuestasRepository,
    val appState: AppState,
    val usuarioDao: UsuarioDao
) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EncuestaCovidViewModel::class.java)) {
            return EncuestaCovidViewModel(
                encuestaId,
                encuestasRepository,
                appState,
                usuarioDao
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}