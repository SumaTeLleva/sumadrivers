package mx.suma.drivers.bluetooth

import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import mx.suma.drivers.MainActivity
import mx.suma.drivers.R
import mx.suma.drivers.models.db.*
import mx.suma.drivers.utils.formatAsDateTimeString
import mx.suma.drivers.utils.now
import mx.suma.drivers.work.EnviarDatosAforosWorker
import timber.log.Timber
import java.util.*
import kotlin.concurrent.timerTask

class CapturaAforosService : Service(), Bluetooth.CommunicationCallback {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var bluetooth: Bluetooth
    private lateinit var bluetoothManager: BluetoothManager

    private var startId: Int = 0
    private var pld: Boolean = false
    private var ts = ""
    private var fn = ""
    private var tc = 0
    private var cc = 0
    private var id = 0

    private var _readerName: String = "___"
    private val readerName: String
        get() = _readerName.md5()

    private var connectionAttemps = 4
    private var currentConnectionAttemps = 0

    private val ONGOING_NOTIFICATION_ID: Int = 101
    private val CHANNEL_DEFAULT_IMPORTANCE: String = "bt_channel"

    private var bundle: Bundle? = null
    private var timer: Timer? = null
    private var bitacoraModel: BitacoraModel = BitacoraModel()
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var lastPing: Long = -1L
    private var sessionStarted: Long = -1L
    private var isAlive: Long = -1L
    private var sessionEnded: Long = -1L

    companion object {
        const val READER_NAME_KEY: String = "reader"
        const val BITACORA_KEY: String = "bitacora"

        const val CMD_ENVIAR_MINUTIAES = "enviar_minutiaes"
        const val CMD_SET_TIME = "set_time_%s"
        const val CMD_PING = "ping"

        const val MSJ_TIEMPO_ACTUALIZADO = "tiempo_actualizado"
        const val MSJ_CODIGO_SESION_GUARDADO = "codigo_sesion_guardado"
        const val MSJ_INICIANDO_ENVIO_MINUTIAES = "iniciando_envio_minutiaes"
        const val MSJ_MINUTIAES_ENVIADAS = "minutiaes_enviadas"
        const val MSJ_REGISTROS_ENVIADOS = "registros_enviados"
        const val MSJ_APAGANDO_LECTOR = "apagando_lector"
        const val MSJ_REGISTRO_INICIADO = "registro_iniciado"
        const val MSJ_REGSITRO_FINALIZADO = "registro_finalizado"
        const val MSJ_PONG = "pong"

        const val RGX_RECEIVE_PAYLOAD_INFO = "^pld_\\d+$"
        const val RGX_START_PAYLOAD_RECEPTION = "^pld_\\w+.\\w+.\\w+_\\d+_\\d+\$"
        const val RGX_START_ENROLLMENT_RECEPTION = "^pld_rutas-\\w+.\\w+.\\w+_\\d+_\\d+\$"
        const val RGX_TEST = "^tst_.+\$"
    }

    override fun onCreate() {
        bluetoothManager = this.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if(Build.VERSION.SDK_INT <= 32 ){
            bluetooth = Bluetooth(BluetoothAdapter.getDefaultAdapter(), this)
        }
        else {
            bluetooth = Bluetooth(bluetoothManager.adapter, this)
        }
        firebaseAnalytics = Firebase.analytics
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("Starting service...")
        this.startId = startId
        _readerName = intent.extras?.getString(READER_NAME_KEY, "suma02") as String

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = CHANNEL_DEFAULT_IMPORTANCE
                val name = "bt"
                val importance = NotificationManager.IMPORTANCE_LOW

                notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(
                    NotificationChannel(channel, name, importance))
            }

            val pendingIntent: PendingIntent =
                Intent(this, MainActivity::class.java).let {
                    PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
                }

            notificationBuilder = NotificationCompat.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_bluetooth_searching)
                .setContentIntent(pendingIntent)


            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ServiceCompat.startForeground(this,100, notificationBuilder.build(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
                )
            }else {
                startForeground(ONGOING_NOTIFICATION_ID, notificationBuilder.build())
            }

            bundle = intent.extras

            handleBundle()
        }catch (e:Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException
            ) {
                Timber.e(e)
            }
            Timber.e(e)
        }

        firebaseAnalytics.logEvent("captura_aforos_service_started") {
            param("dt", now().time.formatAsDateTimeString())
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    private fun handleBundle() {
        if(bundle == null)
            throw IllegalStateException("No bundle")

        bundle?.let {
            val bitacora = it.getParcelable(BITACORA_KEY) as BitacoraModel?
                ?: throw IllegalStateException("Faltó la bitácora en el Bundle!")

            when {
                bitacora.capturarAforo.not() -> {
                    Timber.d("No se requiere captura de Aforo para este servicio")
                    stopService()
                }
                bluetooth.deviceIsBonded(readerName).not() -> {
                    Timber.d("Not bonded! :(")

                    Toast.makeText(this, "Lector de Aforo no vinculado", Toast.LENGTH_LONG).show()
                    stopService()
                }
                bluetooth.isEnabled.not() -> {
                    Timber.d("Bluetooth is not enabled! :(")

                    Toast.makeText(this, "Bluetooth no está activo", Toast.LENGTH_LONG).show()
                    stopService()
                }
                else -> {
                    Timber.d("INIT")
                    init(bitacora)
                    startTimer()
                }
            }
        }
    }

    private fun init(bitacora: BitacoraModel) {
        if (bitacoraModel.id == -1L && bitacora.inTime()) {
            bitacoraModel = bitacora
        } else if(bitacoraModel.id == bitacora.id) {
            bitacoraModel = bitacora
        } else {
            Timber.d("Skipping data...")
        }
    }

    private fun startTimer() {
        if(timer == null && bitacoraModel.id != -1L) {
            timer = Timer()

            timer?.scheduleAtFixedRate(timerTask {
                if (bitacoraModel.inTime().not()) {
                    Timber.d("Not in time, finish")
                    sessionEnded = System.currentTimeMillis()
                }

                if(bluetooth.isConnected.not()) {
                    Timber.d("Attempting connection")
                    bluetooth.connectToName(readerName)
                } else {
                    onConnect()
                }
            }, 0, 60000)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        Timber.d("Service done!")
        bluetooth.disconnect()
        timer?.cancel()
    }

    private fun stopService() {

        firebaseAnalytics.logEvent("captura_aforos_service_stopped") {
            param("dt", now().time.formatAsDateTimeString())
        }

        stopForeground(true)
        stopSelf(startId)
    }

    override fun onConnect() {
        when {
            lastPing == -1L -> {
                bluetooth.send(CMD_PING)
            }
            sessionStarted == -1L -> {
                bluetooth.send(CMD_SET_TIME.format(System.currentTimeMillis()))
                Thread.sleep(2000)
                bluetooth.send(bitacoraModel.getInicioSesion())
            }
            sessionEnded > 0 -> {
                bluetooth.send(CMD_ENVIAR_MINUTIAES)
            }
            isAlive < System.currentTimeMillis() -> {
                bluetooth.send(CMD_PING)
            }
            else -> {
                Timber.d("What now?")
            }
        }

        updateNotification(75)
    }

    private fun updateNotification(progress: Int, icon: Int = R.drawable.ic_fingerprint) {
        notificationBuilder.apply {
            setSmallIcon(icon)
            setProgress(100, progress, false)
            setContentText("Conexión exitosa")
        }

        notificationManager.notify(ONGOING_NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onDisconnect(message: String) {
        Timber.d("Disconnected")
    }

    override fun onMessage(message: String) {
        val msj = message.split("###")[0]

        //Timber.d("Got message! --> $msj")

        updateNotification(100)

        when(msj) {
            MSJ_TIEMPO_ACTUALIZADO -> {
                // updateNotification(60)
                Timber.d("Tiempo actualizado")
            }
            MSJ_CODIGO_SESION_GUARDADO -> {
                sessionStarted = System.currentTimeMillis()
                // bluetooth.disconnect()
            }
            MSJ_INICIANDO_ENVIO_MINUTIAES -> {
            }
            MSJ_MINUTIAES_ENVIADAS -> {
                stopService()
            }
            MSJ_REGISTROS_ENVIADOS -> {
                //bluetooth.send("apagar")
            }
            MSJ_APAGANDO_LECTOR -> {
            }
            MSJ_REGISTRO_INICIADO -> {
                //Thread.sleep(5000)
                //bluetooth.disconnect()
            }
            MSJ_REGSITRO_FINALIZADO -> {
                //bluetooth.send("apagar")
            }
            MSJ_PONG -> {
                if(lastPing == -1L) {
                    lastPing = System.currentTimeMillis()
                    this.onConnect()
                } else {
                    Timber.d("Is alive")
                    isAlive = System.currentTimeMillis()

                    // bluetooth.disconnect()
                }
            }
            in Regex(RGX_RECEIVE_PAYLOAD_INFO) -> {
                pld = true
                cc = msj.split("_")[1].toInt()
            }
            in Regex(RGX_START_PAYLOAD_RECEPTION) -> {
                pld = true

                val parts = msj.split("_")

                fn = parts[1]
                cc = parts[2].toInt()
                id = parts[3].toInt()
            }
            in Regex(RGX_START_ENROLLMENT_RECEPTION) -> {
                pld = true

                val parts = msj.split("_")

                fn = parts[1]
                cc = parts[2].toInt()
                id = parts[3].toInt()
            }
            in Regex(RGX_TEST) -> {
                Timber.d("Results: %s".format(msj))

                //bluetooth.disconnect()
            }
            else -> {
                if (pld) {
                    ts += msj
                    tc++

                    if(tc ==  cc) {
                        val tmp1 = ts
                        saveImage(tmp1)

                        pld = false
                        ts = ""
                        tc = 0
                        cc = 0

                        Thread.sleep(5000)

                        if(id > 0) {
                            bluetooth.send("enviar_minutiaes")
                        }
                    }
                } else {
                    Timber.d("Some other message: $msj")
                }
            }
        }
    }

    override fun onError(message: String) {
        Timber.d("Error: $message")
        firebaseAnalytics.logEvent("captura_aforos_error") {
            param("dt", now().time.formatAsDateTimeString())
            param("msj", message)
            param("unidad_id", bitacoraModel.idUnidad)
            param("operador_id", bitacoraModel.idOperador)
            param("proveedor_id", bitacoraModel.idProveedor)
        }

        stopService()
    }

    override fun onConnectError(message: String) {
        Timber.d("Connection attempt error. Attempts: $currentConnectionAttemps")

        if(currentConnectionAttemps > connectionAttemps) {
            stopService()
        }

        firebaseAnalytics.logEvent("captura_aforos_connection_error") {
            param("dt", now().time.formatAsDateTimeString())
            param("unidad_id", bitacoraModel.idUnidad)
            param("operador_id", bitacoraModel.idOperador)
            param("proveedor_id", bitacoraModel.idProveedor)
        }

        currentConnectionAttemps++
        Thread.sleep(5000)
    }

    override fun getContext(): Context {
        return this
    }

    private fun saveImage(imageData: String) {
        Timber.d(imageData)

        val imgBytesData = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)

        Timber.d("About to save to file")

        getContext().openFileOutput(fn, Context.MODE_PRIVATE).use {
            it.write(imgBytesData)
        }

        val theWorker = OneTimeWorkRequestBuilder<EnviarDatosAforosWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(theWorker)
    }
}