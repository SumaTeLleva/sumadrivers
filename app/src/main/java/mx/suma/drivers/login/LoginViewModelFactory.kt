package mx.suma.drivers.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.suma.drivers.database.UsuarioDao
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.session.AppState

class LoginViewModelFactory(
    private val service: ApiSuma,
    private val appState: AppState,
    private val datasource: UsuarioDao) : ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(service, appState, datasource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}