package mx.suma.drivers.login

import androidx.lifecycle.*
import kotlinx.coroutines.*
import mx.suma.drivers.R
import mx.suma.drivers.database.UsuarioDao
import mx.suma.drivers.models.api.BitacoraDate
import mx.suma.drivers.models.api.usuarios.Usuario
import mx.suma.drivers.models.api.utils.asDbModel
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.network.Resource
import mx.suma.drivers.network.ResponseHandler
import mx.suma.drivers.network.Status
import mx.suma.drivers.session.AppState
import mx.suma.drivers.utils.getRequestBodyFromPayload
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.Exception

class LoginViewModel(
    private val service: ApiSuma,
    private val appState: AppState,
    private val datasource: UsuarioDao) : ViewModel() {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _loginWithPhoneNumber = MutableLiveData<Boolean>(true)
    val loginWithPhoneNumber: LiveData<Boolean>
        get() = _loginWithPhoneNumber

    private val _loginWithEmailPassword = MutableLiveData<Boolean>(false)
    val loginWithEmailAndPassword: LiveData<Boolean>
        get() = _loginWithEmailPassword

    private val _isSendingRequest = MutableLiveData<Boolean>(false)
    val isSendingRequest: LiveData<Boolean>
        get() = _isSendingRequest

    val errorMessage = MutableLiveData<String>("")
    val showError: LiveData<Boolean> = Transformations.map(errorMessage) {
        it.isNotEmpty()
    }

    val controlsEnabled: LiveData<Boolean> = Transformations.map(_isSendingRequest) {
        it.not()
    }

    private val _navigateToStarterApp = MutableLiveData<Boolean>(false)
    val nvigateToStarterApp: LiveData<Boolean>
        get() = _navigateToStarterApp

    val usuario = MutableLiveData<Usuario>()

    val title = Transformations.map(_loginWithPhoneNumber) {
        if (it) {
            R.string.acceder_con_numero
        } else {
            R.string.acceder_con_email
        }
    }

    val btnTitle = Transformations.map(_loginWithPhoneNumber) {
        if (it) {
            R.string.acceder_con_email_y_password
        } else {
            R.string.acceder_con_numero
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onToggleLoginMode() {
        _loginWithPhoneNumber.value = _loginWithPhoneNumber.value?.not()
        _loginWithEmailPassword.value = _loginWithEmailPassword.value?.not()
    }

    fun submitLoginData(phoneNumber: String?, email: String?, password: String?) {
        _isSendingRequest.value = true
        errorMessage.value = ""

        uiScope.launch {

            val payload = JSONObject()

            if(loginWithPhoneNumber.value == true) {
                payload.put("linea_telefono", phoneNumber)
            } else {
                payload.put("email", email)
                payload.put("password", password)
            }

            val rFecha = obtenerFechaActual()
            val result = sendData(payload)


            when(result.status) {
                Status.SUCCESS -> {
                    Timber.i("Success! Save data and go to main activity")
                    val formtat = SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss", Locale.ROOT).parse(rFecha.data!!.data.FECHA_HORA)
                    val fechaActual = Calendar.getInstance()
                    fechaActual.time = formtat
                    fechaActual.add(Calendar.DATE, 15)

                    result.data?.let {
                        saveUserData(it.asDbModel())
                        usuario.value = it
                        appState.marcarAutenticado()
                        appState.setDateSession(fechaActual.timeInMillis)
                        _navigateToStarterApp.value = true
                    }
                }
                Status.ERROR -> {
                    result.message?.let {
                        errorMessage.value = it
                    }

                }
                Status.LOADING -> Timber.i("Loading...")
            }

            _isSendingRequest.value = false
        }
    }

    private suspend fun obtenerFechaActual(): Resource<BitacoraDate> {
        return withContext(Dispatchers.IO) {
            try {
                ResponseHandler.handleSuccess(service.getFechaHora())
            }catch (e: Exception) {
                Timber.e(e)
                ResponseHandler.handleException<BitacoraDate>(e)
            }
        }
    }

    private suspend fun sendData(payload: JSONObject): Resource<Usuario> {
        val body = getRequestBodyFromPayload(payload)

        return withContext(Dispatchers.IO) {
            try {
                ResponseHandler.handleSuccess(service.postLogin(body))
            } catch (e: Exception) {
                Timber.e(e)
                ResponseHandler.handleException<Usuario>(e)
            }
        }
    }

    private suspend fun saveUserData(usuarioDb: UsuarioModel) {
        withContext(Dispatchers.IO) {
            datasource.insert(usuarioDb)
        }
    }

    fun onNavigationComplete() {
        _navigateToStarterApp.value = false
    }
}
