package mx.suma.drivers.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Long.parseLong
import java.util.*

class Bluetooth(
    private val bluetoothAdapter: BluetoothAdapter,
    val listener: CommunicationCallback
) {

    var socket: BluetoothSocket? = null
        private set
    var device: BluetoothDevice? = null
        private set

    private var input: InputStream? = null
    private var out: OutputStream? = null

    private var n: Int = parseLong("200", 16).toInt()

    var isConnected = false
        private set
    private var isConnecting = false
    private var isBonded = false

    val isEnabled: Boolean
        get() {
            return bluetoothAdapter.isEnabled
        }

    private val pairedDevices: List<BluetoothDevice>
        @SuppressLint("MissingPermission")
        get() {
            return bluetoothAdapter.bondedDevices.toList()
        }

    private var readerName = ""

    private fun connectToAddress(address: String) {
        if (isConnected) {
            throw IllegalStateException("How do we get here? WTF")
        }

        val device = bluetoothAdapter.getRemoteDevice(address)
            ?: throw IllegalStateException("Device is null!")

        ConnectThread(device).start()

        isConnected = false
        isConnecting = true

        Timber.d("Starting connection for $address")
    }

    fun connectToName(name: String) {
        readerName = name

        Timber.d("Searching on bonded devices. Total: %s", pairedDevices.size)

        Timber.d("isConnected: %s - isConnecting:%s", isConnected, isConnecting)

        if (!bluetoothAdapter.isEnabled) {
            Timber.d("Bluetooth is not enabled!")
            throw IllegalStateException("Bluetooth must be enabled by user!")
        } else if (isConnecting) {
            Timber.d("Ongoing connection attempt!")
        } else if (isConnected) {
            Timber.d("Connection already established")
            listener.onConnect()
        } else if (deviceIsBonded(readerName)) {
            Timber.d("Connecting to %s", readerName)

            isPairedDevice(readerName).map {
                connectToAddress(it.address)
            }
        } else {
            Timber.d("Device not paired: $readerName")
            listener.onError("El dispositivo se encuentra vinculado vinculado ;( ($readerName)")
        }
    }

    fun deviceIsBonded(rpiName: String): Boolean {
        isBonded = isPairedDevice(rpiName).isNotEmpty()

        return isBonded
    }

    @SuppressLint("MissingPermission")
    private fun isPairedDevice(name: String): List<BluetoothDevice> {
        return pairedDevices.filter { it.name == name }
    }

    fun disconnect() {
        try {
            socket?.close()

            isConnected = false
            isConnecting = false
        } catch (e: IOException) {
            listener.onError(e.message.toString())
        }
    }

    fun send(msg: String) {
        try {
            out?.write(msg.toByteArray())
        } catch (e: IOException) {
            isConnected = false
            listener.onDisconnect(e.message.toString())
        }
    }

    private inner class ReceiveThread : Thread(), Runnable {
        override fun run() {
            try {
                while (true) {
                    val bytes = ByteArray(n)

                    input?.read(bytes, 0, n)

                    listener.onMessage(String(bytes, 0, n))
                }
            } catch (e: IOException) {
                isConnected = false
                isConnecting = false

                listener.onDisconnect(e.message.toString())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        init {
            this@Bluetooth.device = device
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
                listener.onError(e.message.toString())
            }
        }

        override fun run() {
            try {
                socket?.connect()
                out = socket?.outputStream
                input = socket?.inputStream
                isConnected = true
                isConnecting = false

                ReceiveThread().start()

                listener.onConnect()
            } catch (e: IOException) {
                isConnected = false
                isConnecting = false

                Timber.d(e.message.toString())
                listener.onConnectError(e.message.toString())
            }
        }
    }

    interface CommunicationCallback {
        fun onConnect()
        fun onDisconnect(message: String)
        fun onMessage(message: String)
        fun onError(message: String)
        fun onConnectError(message: String)
        fun getContext(): Context
    }

    companion object {
        private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}