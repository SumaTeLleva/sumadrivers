package mx.suma.drivers.mantenimientos.listado

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import mx.suma.drivers.R
import mx.suma.drivers.models.db.MantenimientoModel

@BindingAdapter("fechaMantenimiento")
fun TextView.setFechaMantenimiento(item: MantenimientoModel) {
    item.let {
        text = item.fecha
    }
}

@BindingAdapter("tituloMantenimiento")
fun TextView.setTituloMantenimiento(item: MantenimientoModel) {
    item.let {
        text = item.titulo
    }
}

@BindingAdapter("comentarioMantenimiento")
fun TextView.setComentarioMantenimiento(item: MantenimientoModel) {
    item.let {
        text = item.notas
    }
}

@BindingAdapter("idMantenimiento")
fun TextView.setIdMantenimiento(item: MantenimientoModel) {
    item.let {
        text = "Id: ${item.id}"
    }
}

@BindingAdapter("estatusMantenimiento")
fun ImageView.setEstatusMantenimiento(item: MantenimientoModel) {
    item.let {
        if(it.concluido) {
            setImageResource(R.drawable.ic_baseline_check_circle_green)
        } else {
            setImageResource(R.drawable.ic_baseline_warning)
        }
    }
}