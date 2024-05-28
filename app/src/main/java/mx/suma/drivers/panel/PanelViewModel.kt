package mx.suma.drivers.panel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import mx.suma.drivers.BuildConfig
import mx.suma.drivers.database.UsuarioDao
import mx.suma.drivers.models.api.BitacoraDate
import mx.suma.drivers.models.api.archivo.Avisos
import mx.suma.drivers.models.db.Dispositivo
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.network.ApiSuma
import mx.suma.drivers.network.Resource
import mx.suma.drivers.network.ResponseHandler
import mx.suma.drivers.network.Status
import mx.suma.drivers.repositories.archivos.ArchivosRepository
import mx.suma.drivers.repositories.dispositivos.DispositivosRepository
import mx.suma.drivers.session.AppState
import timber.log.Timber
import java.net.SocketException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.Exception

class PanelViewModel(
    val appState: AppState,
    val usuarioDao: UsuarioDao,
    val dispositivosRepository: DispositivosRepository,
    private val service: ApiSuma,
    val archivosRepository: ArchivosRepository
) : ViewModel() {

    val viewModelJob = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var registrandoDispositivo = false

    val versionName = MutableLiveData("v0.0")

    val navigateToTickets = MutableLiveData(false)
    val navigateToBitacoras = MutableLiveData(false)
    val navigateToMantenimientos = MutableLiveData(false)
    val navigateToDirectorio = MutableLiveData(false)
    val navigateToAuditorias = MutableLiveData(false)
    val navigateToKitLimpieza = MutableLiveData(false)
    val navigateToOtherKit = MutableLiveData(false)
    val navigateToMiUnidad = MutableLiveData(false)
    val navigateToStarterFragment = MutableLiveData(false)
    val navigateToSanitizaciones = MutableLiveData(false)
    val navigateToEncuestaCovid = MutableLiveData(false)
    val avisosImages = MutableLiveData<ArrayList<String>>()
    val navigateToSolDesplazamiento = MutableLiveData(false)

    init {
        versionName.value = BuildConfig.VERSION_NAME
        uiScope.launch{
            try {
                val avisos = obtenerAvisos()
                avisosImages.value = avisos.archivos
            }catch (e: Exception) {
                Timber.e(e)
            }catch (e: SocketException) {
                Timber.e(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onNavigationComplete() {
        navigateToStarterFragment.value = false
        navigateToTickets.value = false
        navigateToAuditorias.value = false
        navigateToBitacoras.value = false
        navigateToMantenimientos.value = false
        navigateToDirectorio.value = false
        navigateToKitLimpieza.value = false
        navigateToOtherKit.value = false
        navigateToMiUnidad.value = false
        navigateToSanitizaciones.value = false
        navigateToEncuestaCovid.value = false
        navigateToSolDesplazamiento.value = false
    }

    fun onNavigateToTickets() {
        navigateToTickets.value = true
    }

    fun onNavigateToBitacoras() {
        navigateToBitacoras.value = true
    }

    fun onNavigateToMantenimientos() {
        navigateToMantenimientos.value = true
    }

    fun onNavigateToDirectorio() {
        navigateToDirectorio.value = true
    }

    fun onNavigateToAuditorias() {
        navigateToAuditorias.value = true
    }

    fun onNavigateToKitLimpieza() {
        navigateToKitLimpieza.value = true
    }

    fun onNavigateToSolDesplazamiento() {
        navigateToSolDesplazamiento.value = true
    }

    fun onNavigationToOthersKit() {
    navigateToOtherKit.value = true
    }

    fun onNavigateToMiUnidad() {
        navigateToMiUnidad.value = true
    }

    fun onNavigateToSanitizaciones() {
        navigateToSanitizaciones.value = true
    }

    fun onNavigateEncuestaCovid() {
        navigateToEncuestaCovid.value = true
    }

    fun clearSession() {
        uiScope.launch {
            // TODO: Devolver funcionalidad normal
            appState.invalidarSesion()

            withContext(Dispatchers.IO) {
                usuarioDao.clear()
            }

            /*appState.setFechaUltimaEncuesta("")
            withContext(Dispatchers.IO) {
                usuario.value?.acceso = true

                usuarioDao.insert(usuario.value as UsuarioModel)
            }*/

            navigateToStarterFragment.value = true
        }
    }

    suspend fun obtenerAvisos(): Avisos {
        return archivosRepository.obtenerAvisos()
    }

    suspend fun esSesionCaducado(): Boolean {
        var haCaducado: Boolean = false
        if(appState.getDateSession() > 0)
        {
            try {
                val rFecha = obtenerFechaActual()

                when(rFecha.status) {
                    Status.SUCCESS -> {
                        var fecha = Calendar.getInstance()
                        val formtat = SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss", Locale.ROOT).parse(rFecha.data!!.data.FECHA_HORA)
                        val fechaActual = Calendar.getInstance()
                        fechaActual.time = formtat
                        fecha.timeInMillis = appState.getDateSession()
                        println("Fecha actual ${fechaActual.time} - Fecha guardado ${fecha.time}")
                        haCaducado = fecha.before(fechaActual)
                    }
                    Status.ERROR -> {
                        Timber.i("Ocurrio un problema interno")
                    }
                    else -> {
                        // TODO("nada")
                    }
                }
            }catch (e:Exception) {
                Timber.i(e)
            }
        } else {
            Timber.i("No existe ningun dato almacenado de inicio de sesión")
        }
        return haCaducado
    }


    private suspend fun obtenerFechaActual():Resource<BitacoraDate> {
        return withContext(Dispatchers.IO) {
            try {
                ResponseHandler.handleSuccess(service.getFechaHora())
            }catch (e: Exception) {
                ResponseHandler.handleException(e)
            }
        }
    }

    fun handleToken(token: String, usuario: UsuarioModel) {
        uiScope.launch {
            appState.setFirebaseToken(token)

            val dispositivo = Dispositivo.createDispositivo(
                idUsuario = usuario.id,
                firebaseInstanceId = token
            )

            try {
                if (appState.getDispositivoId() == -1L) {
                    val response =
                        dispositivosRepository.suscribirDispositivo(dispositivo.getPayload())

                    appState.setDispositivoId(response.data.id)

                    Timber.i("Suscripción de dispositivo exitosa")
                }
            } catch (e: Exception) {
                Timber.e("Ocurrió un error ${e.message}")
            }
        }
    }
}
