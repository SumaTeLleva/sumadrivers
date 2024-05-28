package mx.suma.drivers.bitacoras.listado

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import mx.suma.drivers.R
import mx.suma.drivers.models.db.BitacoraModel
import mx.suma.drivers.models.utils.EstatusServicio
import mx.suma.drivers.models.utils.EstatusServicio.Estatus.*
import mx.suma.drivers.utils.LogosClientes
import mx.suma.drivers.utils.formatDateAsDateString
//import mx.suma.drivers.utils.formatDateAsDateString
import mx.suma.drivers.utils.formatDateAsTimeAmPmString
import mx.suma.drivers.utils.parseDateTimeString

@BindingAdapter("idBitacora")
fun TextView.setBitacoraId(item: BitacoraModel?) {
    item?.let {
//        text = "ID: ${it.id}"
        text = String.format("ID: %1d", it.id)
    }
}

@BindingAdapter("folioBitacora")
fun TextView.setFolioBitacora(item: BitacoraModel?) {
    item?.let {
//        text = "F: ${it.folioBitacora}"
        text = String.format("F: %1d", it.folioBitacora)
    }
}

@BindingAdapter("kilometrajesBitacora")
fun TextView.setKilometrajes(item: BitacoraModel?) {
    item?.let {
//        text =  "Km: ${it.kilometrajeInicial} a ${it.kilometrajeFinal}"
        text = String.format("Km: %1s a %2s", it.kilometrajeInicial, it.kilometrajeFinal)
    }
}

@BindingAdapter("fechaBitacora")
fun TextView.setFechaBitacora(item: BitacoraModel?) {
    item?.let {
        text = parseDateTimeString(it.fecha).time.formatDateAsDateString()
    }
}

@BindingAdapter("horariosBitacora")
fun TextView.setTiempos(item: BitacoraModel?) {
    item?.let {
        val inicial = parseDateTimeString(it.tiempoInicial).time
        val final = parseDateTimeString(it.tiempoFinal).time
//        text = "De: ${inicial.formatDateAsTimeAmPmString()} a ${final.formatDateAsTimeAmPmString()}"
        text = String.format("De: %1s a %2s",inicial.formatDateAsTimeAmPmString(), final.formatDateAsTimeAmPmString())
    }
}

@BindingAdapter("personasBitacora")
fun TextView.setPersonas(item: BitacoraModel?) {
    item?.let {
        text = it.numeroPersonas.toString()
    }
}

@BindingAdapter("logoEmpresa")
fun ImageView.setLogoEmpresa(item: BitacoraModel?) {
    item?.let {
        setImageResource(LogosClientes.getLogo(item.idCliente))
    }
}

@BindingAdapter("estatusServicio")
fun ImageView.setEstatusServicio(item: BitacoraModel?) {
    item?.let {
        // TODO: Refactor into a function
        val estatus = when(EstatusServicio.getStatus(item)) {
            CONFIRMAR_SERVICIO
                -> R.drawable.ic_servicio_confirmar
            SERVICIO_NO_CONFIRMABLE
                -> R.drawable.ic_servicio_no_confirmado
            ABRIR_BITACORA, INICIAR_RUTA, TERMINAR_RUTA, CERRAR_BITACORA
                -> R.drawable.ic_servicio_en_ruta
            SERVICIO_TERMINADO
                -> R.drawable.ic_servicio_terminado
            else
                -> R.drawable.ic_servicio_programado
        }

        setImageResource(estatus)
    }
}