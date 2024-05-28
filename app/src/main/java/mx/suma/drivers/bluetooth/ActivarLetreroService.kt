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
import mx.suma.drivers.MainActivity
import mx.suma.drivers.R
import timber.log.Timber

class ActivarLetreroService : Service(), Bluetooth.CommunicationCallback {

    private lateinit var bluetooth: Bluetooth
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var bluetoothManager: BluetoothManager

    private var startId: Int = 0

    private val ONGOING_NOTIFICATION_ID: Int = 102
    private val CHANNEL_DEFAULT_IMPORTANCE: String = "bt_channel_letrero"

    private var _readerName: String = "suma10_leds"

    private val readerName: String
        get() = _readerName

    private var _nombreRuta: String = "RUTA DE PRUEBA"
    private var _mode: String = MOSTRAR_LETRERO

    private lateinit var bundle: Bundle

    companion object {
        const val READER_NAME_KEY: String = "reader"
        const val NOMBRE_RUTA_KEY: String = "nombre_ruta"
        const val MODE_KEY: String = "mode"

        const val MOSTRAR_LETRERO: String = "mostrar_letrero"
    }

    override fun onCreate() {
        bluetoothManager = this.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if(Build.VERSION.SDK_INT <= 32 ){
            bluetooth = Bluetooth(BluetoothAdapter.getDefaultAdapter(), this)
        }
        else {
            bluetooth = Bluetooth(bluetoothManager.adapter, this)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("Starting service...")
        this.startId = startId

        bundle = requireNotNull(intent.extras)

        _readerName = bundle.getString(READER_NAME_KEY, _readerName)
        _nombreRuta = bundle.getString(NOMBRE_RUTA_KEY, _nombreRuta)
        _mode = bundle.getString(MODE_KEY, _mode)


        try {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
                val channel = CHANNEL_DEFAULT_IMPORTANCE
                val name = "bt1"
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(
                    NotificationChannel(channel, name, importance)
                )
            }

            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            notificationBuilder = NotificationCompat.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                .setContentTitle(getText(R.string.notification_letrero_title))
                .setContentText(getText(R.string.notification_letrero_message))
                .setSmallIcon(R.drawable.ic_bluetooth_searching)
                .setContentIntent(pendingIntent)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ServiceCompat.startForeground(this,100, notificationBuilder.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
                )
            }else {
                startForeground(ONGOING_NOTIFICATION_ID, notificationBuilder.build())
            }
        }catch (e: Exception){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException) {
                Timber.e(e)
            }
            Timber.e(e)
        }

        handleBundle()

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY
    }

    private fun handleBundle() {
        when {
            bluetooth.isEnabled.not() -> {
                Toast.makeText(this, "Bluetooth no se encuentra activado", Toast.LENGTH_LONG).show()
                stopService()
            }
            bluetooth.deviceIsBonded("$readerName ") -> {
                // Para el HC-05 le agrega un espacio al final del nombre
                Toast.makeText(this, "Conectando a Arduino", Toast.LENGTH_LONG).show()
                bluetooth.connectToName("$readerName ")
            }
            bluetooth.deviceIsBonded("Conectando a ${readerName}_pi") -> {
                // Handles RPi cases
                Toast.makeText(this, "RPi", Toast.LENGTH_LONG).show()
                bluetooth.connectToName("${readerName}_pi")
            }
            bluetooth.deviceIsBonded("${readerName}_pi").not() && bluetooth.deviceIsBonded("$readerName ").not() -> {
                Toast.makeText(this, "Lo sentimos, el letrero no está vinculado", Toast.LENGTH_LONG).show()
                stopService()
            }
            else -> {
                Timber.d("Nothing to do")
                stopService()
            }
        }
    }

    private fun updateNotification(progress: Int, icon: Int = R.drawable.ic_smart_button_24px) {
        notificationBuilder.apply {
            setSmallIcon(icon)
            setProgress(100, progress, false)
            setContentText("Conexión via bluethooth exitosa")
        }

        notificationManager.notify(ONGOING_NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onDestroy() {
        Timber.d("Service done!")
        bluetooth.disconnect()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun stopService() {
        stopForeground(true)
        stopSelf(startId)
    }

    override fun onConnect() {
        Timber.d("Connected")
        when (_mode) {
            MOSTRAR_LETRERO -> {
                Timber.d("Mostrar letrero")
                if(bluetooth.deviceIsBonded("${readerName}_pi")) {
                    bluetooth.send("mostrar_letrero:$_nombreRuta")
                } else if(bluetooth.deviceIsBonded("$readerName ")) {
                    bluetooth.send(_nombreRuta)
                }
            }
        }
    }

    override fun onDisconnect(message: String) {
        Timber.d("Disconnected")
        stopService()
    }

    override fun onMessage(message: String) {
        val msj = message.split("###")[0]

        Timber.d("Got message! --> $msj")

        when (msj) {
            "mostrando_letrero" -> {
                Timber.d("Se muestra letrero")
                updateNotification(100)
                bluetooth.disconnect()
            }
            else -> {
                Timber.d("Some other message: $msj")
                bluetooth.disconnect()
            }
        }
    }

    override fun onError(message: String) {
        Timber.d("Error: $message")
        stopService()
    }

    override fun onConnectError(message: String) {
        Timber.d("Connect Error: $message")
        stopService()
    }

    override fun getContext(): Context = this.getContext()
}