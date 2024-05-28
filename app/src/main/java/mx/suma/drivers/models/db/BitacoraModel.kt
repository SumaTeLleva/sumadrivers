package mx.suma.drivers.models.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import mx.suma.drivers.utils.formatAsDateTimeString
import mx.suma.drivers.utils.now
import mx.suma.drivers.utils.parseDateTimeString
import org.json.JSONObject
import timber.log.Timber

@Parcelize
@Entity(tableName = "bitacoras")
data class BitacoraModel(
    @PrimaryKey var id: Long = -1L,
    var modalidad: String = "",
    var terminado: Boolean = false,
    var confirmado: Boolean = false,
    var motivoTransferencia: String = "",
    var transferido: Boolean = false,
    var transferidoAt: String? = null,
    var tiempoInicial: String = "",
    var tiempoFinal: String = "",
    var fecha: String = "",
    var alarmaNotificacion: String = "",
    var alarmaInicioRuta: String = "",
    var comentarios: String? = null,
    var estatus: Int = -1,
    var folioBitacora: Long = 0,
    var kilometrajeInicial: Long = 0,
    var kilometrajeFinal: Long = 0,
    var numeroPersonas: Int = 0,
    var tiempoCantidad: String = "",
    var dia: Int = -1,
    var semana: Int = -1,
    var idProveedor: Long = -1,
    var idOperador: Long = -1,
    var idUnidad: Long = -1,
    var idRuta: Long = -1,
    var idEstructura: Long = -1,
    var idServicioEspecial: Long = -1,
    var idMapa: Long = -1,
    var horaConfirmacion: String? = null,
    var horaBanderazo: String? = null,
    var horaInicioRuta: String? = null,
    var horaFinalRuta: String? = null,
    var horaCierreRuta: String? = null,
    var tipo: String = "",
    var verificado: Boolean = false,
    var pagarServicio: Boolean = false,
    var cancelado: Boolean = false,
    var canceladoAt: String? = null,
    var excepcion: Boolean = false,
    var excepcionAt: String? = null,
    var nombreRuta: String = "",
    var turno: String = "",
    var idCliente: Long = -1,
    var capturarAforo: Boolean = false,
    var nombreOperador: String = "",
    var letreroEspecial: String = "",
) : Parcelable

fun BitacoraModel.getPayload(): JSONObject {

    val payload = JSONObject()

    if(horaConfirmacion != null) {
        payload.put("hora_confirmacion", horaConfirmacion)
        payload.put("unidad", idUnidad)
    }

    if(horaBanderazo != null) {
        payload.put("hora_banderazo", horaBanderazo)
        payload.put("kilometraje_inicial", kilometrajeInicial)
        payload.put("folio_bitacora", folioBitacora)
    }

    if(horaInicioRuta != null) {
        payload.put("hora_inicio_ruta", horaInicioRuta)
    }

    if(horaFinalRuta != null) {
        payload.put("hora_final_ruta", horaFinalRuta)
        payload.put("numero_personas", numeroPersonas)
    }

    if(horaCierreRuta != null) {
        payload.put("hora_cierre_ruta", horaCierreRuta)
        payload.put("kilometraje_final", kilometrajeFinal)
    }

    if (idRuta == 18L && id == -1L) {
        payload.put("fecha_captura", now().time.formatAsDateTimeString())
        payload.put("fechaCaptura", fecha)
        payload.put("ruta", idRuta)
        payload.put("operador", idOperador)
        payload.put("proveedor", idProveedor)
        payload.put("tiempo_inicial", tiempoInicial)
        payload.put("tiempo_final", tiempoFinal)
        payload.put("tipo", tipo)
        payload.put("estructura", idEstructura)
    }
    println("parameters")
    println(payload)
    return payload
}

fun BitacoraModel.inTime(currentTime: Long = System.currentTimeMillis()): Boolean {
    Timber.d("Current time: $currentTime")

    val inicioMillis = parseDateTimeString(this.horaBanderazo as String).timeInMillis
    var finalMillis = parseDateTimeString(this.tiempoFinal).timeInMillis

    this.horaFinalRuta?.let { finalMillis = parseDateTimeString(it).timeInMillis }

    return (currentTime >= inicioMillis) && (currentTime <= finalMillis)
}

fun BitacoraModel.getInicioSesion(): String {
    val inicio = parseDateTimeString(this.tiempoInicial).timeInMillis
    val final = parseDateTimeString(this.tiempoFinal).timeInMillis
    return "%s_%s_%s_%s".format(this.idRuta, this.id, inicio, final)
}
