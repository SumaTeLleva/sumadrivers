package mx.suma.drivers.models.utils


import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import mx.suma.drivers.models.db.BitacoraModel
import mx.suma.drivers.utils.parseDateTimeString
import java.util.*

object EstatusServicio {

    enum class Estatus {
        SERVICIO_TALLER,
        SERVICIO_PROGRAMADO,
        SERVICIO_TERMINADO,
        SERVICIO_NO_CONFIRMABLE,
        CONFIRMAR_SERVICIO,
        ABRIR_BITACORA,
        INICIAR_RUTA,
        TERMINAR_RUTA,
        CERRAR_BITACORA,
    }

    @SuppressLint("SuspiciousIndentation")
    fun getStatus(bitacora: BitacoraModel): Estatus {
        val alarmaNotificacion = parseDateTimeString(bitacora.alarmaNotificacion).time
        val fin = parseDateTimeString(bitacora.tiempoFinal).time

//        return if (servicioTaller(bitacora, 1400)) {
//            Estatus.SERVICIO_TALLER
//        } else {
            return when {
                servicioConfirmable(bitacora, alarmaNotificacion, fin) -> Estatus.CONFIRMAR_SERVICIO
                servicioNoConfirmable(bitacora, fin) -> Estatus.SERVICIO_NO_CONFIRMABLE
                stepAbrirBitacora(bitacora) -> Estatus.ABRIR_BITACORA
                stepIniciarRuta(bitacora) -> Estatus.INICIAR_RUTA
                stepTerminarRuta(bitacora) -> Estatus.TERMINAR_RUTA
                stepCerrarBitacora(bitacora) -> Estatus.CERRAR_BITACORA
                statusServicioTerminado(bitacora) -> Estatus.SERVICIO_TERMINADO
                else -> Estatus.SERVICIO_PROGRAMADO
            }
//        }
    }

    fun updateTimeByStatus(status:LiveData<Estatus?>, bitacora: BitacoraModel, currentDate: String): BitacoraModel {
        when(status.value) {
            Estatus.CONFIRMAR_SERVICIO -> {
                bitacora.horaConfirmacion = currentDate
            }
            Estatus.ABRIR_BITACORA -> {
                bitacora.horaBanderazo = currentDate
            }
            Estatus.INICIAR_RUTA -> {
                bitacora.horaInicioRuta = currentDate
            }
            Estatus.TERMINAR_RUTA -> {
                bitacora.horaFinalRuta = currentDate
            }
            Estatus.CERRAR_BITACORA -> {
                bitacora.horaCierreRuta = currentDate
            }
            else -> {
                TODO("No realiza ninguna acción")
            }
        }
        return bitacora
    }

    private fun statusServicioTerminado(bitacora: BitacoraModel)
            = bitacora.confirmado && bitacora.terminado

    private fun stepCerrarBitacora(bitacora: BitacoraModel)
            = bitacora.horaFinalRuta != null && bitacora.horaCierreRuta.isNullOrEmpty()

    private fun stepTerminarRuta(bitacora: BitacoraModel)
            = bitacora.horaInicioRuta != null && bitacora.horaFinalRuta.isNullOrEmpty()

    private fun stepIniciarRuta(bitacora: BitacoraModel)
            = bitacora.horaBanderazo != null && bitacora.horaInicioRuta.isNullOrEmpty()

    private fun stepAbrirBitacora(bitacora: BitacoraModel)
            = bitacora.confirmado && bitacora.horaBanderazo.isNullOrEmpty()

    private fun servicioConfirmable(bitacora: BitacoraModel, alarmaNotificacion: Date, fin: Date)
            = !bitacora.confirmado && Date().after(alarmaNotificacion) && Date().before(fin)

    private fun servicioNoConfirmable(bitacora: BitacoraModel, fin: Date)
            =  !bitacora.confirmado && Date().after(fin)
    private fun servicioTaller(bitacora: BitacoraModel, idRuta: Long)
            =  bitacora.idRuta == idRuta
}
