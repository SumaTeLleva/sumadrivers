package mx.suma.drivers.directorio

import android.widget.TextView
import androidx.databinding.BindingAdapter
import mx.suma.drivers.models.db.ContactoModel

@BindingAdapter("nombreContacto")
fun TextView.setNombreContacto(item: ContactoModel?) {
    item?.let{
        text = it.nombre
    }
}

@BindingAdapter("emailContacto")
fun TextView.setEmailContacto(item: ContactoModel?) {
    item?.let {
        text = it.email
    }
}

@BindingAdapter("telefonoContacto")
fun TextView.setTelefonoContacto(item: ContactoModel?) {
    item?.let {
        text = it.telefono
    }
}

@BindingAdapter("tipoContacto")
fun TextView.setTipoContacto(item: ContactoModel?) {
    item?.let{
        text = it.tipo
    }
}