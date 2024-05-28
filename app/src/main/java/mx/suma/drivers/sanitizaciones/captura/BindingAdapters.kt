package mx.suma.drivers.sanitizaciones.captura

import android.widget.TextView
import androidx.databinding.BindingAdapter
import mx.suma.drivers.models.db.BitacoraModel
import mx.suma.drivers.models.db.ClienteModel
import mx.suma.drivers.models.db.UsuarioModel
import mx.suma.drivers.utils.formatDateAsDateString
import java.util.*


@BindingAdapter("nombreEmpresaSanitizacion")
fun TextView.setNombreEmpresaSanitizacion(clienteModel: ClienteModel?) {
    clienteModel?.let {
        text = "${clienteModel.nombreEmpresa}"
    }
}

@BindingAdapter("fechaSanitizacion")
fun TextView.setFechaSanitizacion(fecha: Calendar) {
    text = fecha.time.formatDateAsDateString()
}

@BindingAdapter("nombreOperadorSanitizacion")
fun TextView.setNombreOperadorSanitizacion(bitacoraModel: BitacoraModel?) {
    bitacoraModel?.let {
        text = "${it.nombreOperador}"
    }
}

@BindingAdapter("idUnidadSanitizacion")
fun TextView.setIdUnidadSanitizacion(usuarioModel: UsuarioModel?) {
    usuarioModel?.let {
        text = "U-${it.idUnidad}"
    }
}