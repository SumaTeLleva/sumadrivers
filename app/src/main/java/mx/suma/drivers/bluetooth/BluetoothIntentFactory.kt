package mx.suma.drivers.bluetooth

import android.content.Context
import android.content.Intent
import mx.suma.drivers.models.db.BitacoraModel

class BluetoothIntentFactory {
    companion object {
        fun iniciarCapturaAforo(context: Context, readerName: String, bitacora: BitacoraModel): Intent {

            return Intent(context, CapturaAforosService::class.java).also {

                it.putExtra(CapturaAforosService.READER_NAME_KEY, readerName)
                it.putExtra(CapturaAforosService.BITACORA_KEY, bitacora)
            }
        }

        fun activarLetrero(context: Context, readerName: String, bitacora: BitacoraModel): Intent {
            return Intent(context, ActivarLetreroService::class.java).also {
                it.putExtra(ActivarLetreroService.READER_NAME_KEY, readerName);
                if(bitacora.letreroEspecial.isNullOrEmpty()) {
                    it.putExtra(ActivarLetreroService.NOMBRE_RUTA_KEY, bitacora.nombreRuta);
                }else {
                    it.putExtra(ActivarLetreroService.NOMBRE_RUTA_KEY, bitacora.letreroEspecial);
                }
            }
        }
    }
}