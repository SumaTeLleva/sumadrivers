package mx.suma.drivers.sanitizaciones.listado

import android.widget.TextView
import androidx.databinding.BindingAdapter
import mx.suma.drivers.models.db.SanitizacionModel

@BindingAdapter("idSanitizacion")
fun TextView.setSanitizacionId(sanitizacionModel: SanitizacionModel?) {
    sanitizacionModel?.let {
        text = "Id: ${sanitizacionModel.id}"
    }
}

@BindingAdapter("nombreEmpresa")
fun TextView.setNombreEmpresa(sanitizacionModel: SanitizacionModel?) {
    sanitizacionModel?.let {
        text = sanitizacionModel.nombreCliente
    }
}

@BindingAdapter("fechaRegistro")
fun TextView.setFechaRegistro(sanitizacionModel: SanitizacionModel?) {
    sanitizacionModel?.let {
        text = it.fecha
    }
}