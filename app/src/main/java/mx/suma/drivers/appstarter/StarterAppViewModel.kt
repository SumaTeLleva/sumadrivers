package mx.suma.drivers.appstarter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import mx.suma.drivers.session.AppState
import timber.log.Timber

class StarterAppViewModel(private val appState: AppState) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _showProgressBar = MutableLiveData<Boolean>(true)
    val showProgressBar: LiveData<Boolean>
        get() = _showProgressBar

    private val _isMissingPermissions = MutableLiveData<Boolean>(false)
    val missinPermissions: LiveData<Boolean>
        get() = _isMissingPermissions

    private val _isPermissionPermanentlyDenied = MutableLiveData<Boolean>(false)
    val permissionsDenied: LiveData<Boolean>
        get() = _isPermissionPermanentlyDenied

    private val _isPlayServicesAvailable = MutableLiveData<Boolean>(false)
    val playServicesUnavailable: LiveData<Boolean>
        get() = _isPlayServicesAvailable

    private val _isInternetUnavailable = MutableLiveData<Boolean>(false)
    val isInternetUnavailable: LiveData<Boolean>
        get() = _isInternetUnavailable

    private val _isTestingConnection = MutableLiveData<Boolean>(false)
    val isTestingConnection: LiveData<Boolean>
        get() = _isTestingConnection

    val finishApp = MutableLiveData<Boolean>(false)
    val openAppSettings = MutableLiveData<Boolean>(false)

    val navigateToPanel = MutableLiveData<Boolean>(false)
    val navigateToLogin = MutableLiveData<Boolean>(false)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onShowProgressBar() {
        Timber.i("Showing progress bar")
        _showProgressBar.value = true
    }

    fun onHideProgressBar() {
        Timber.i("Hiding progress bar")
        _showProgressBar.value = false
    }

    fun onMissingPermissions() {
        _isMissingPermissions.value = true
    }

    fun onPermissionsDenied() {
        _isPermissionPermanentlyDenied.value = true
    }

    fun onPlayServicesUnavailable() {
        _isPlayServicesAvailable.value = true
    }

    fun onFinishApp() {
        finishApp.value = true
    }

    fun onOpenAppSettings() {
        openAppSettings.value = true
    }

    fun isConnected() {
        onShowProgressBar()
        _isTestingConnection.value = true

        uiScope.launch {
//            val isConnected = 0
//            if(isConnected == 0) {
//                Timber.i("Ping successful, continue")
                _isInternetUnavailable.value = false

                isAuthenticated()
//            } else {
//                Timber.i("Ping failed")
//                _isInternetUnavailable.value = true
//            }

            onHideProgressBar()
            _isTestingConnection.value = false
        }
    }

    private suspend fun testConnection(): Int {
        val command = "ping -c 1 api.sumaenlinea.mx"

        return withContext(Dispatchers.IO) {

            val result = Runtime.getRuntime().exec(command).waitFor()
//            result = 0
            result
        }
    }

    private fun isAuthenticated() {
        if(appState.autenticado()) {
            Timber.d("Navegar a panel")
            navigateToPanel.value = true
        } else {
            Timber.d("Navegar a pantalla de login")
            navigateToLogin.value = true
        }
    }

    fun onNavigationComplete() {
        navigateToLogin.value = false
        navigateToPanel.value = false
    }
}
