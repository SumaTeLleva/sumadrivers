package mx.suma.drivers

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import mx.suma.drivers.database.UsuarioDao

class SumaDriversViewModel(val usuarioDao: UsuarioDao) : ViewModel() {

    val usuario = usuarioDao.getUsuario()

    val unidadAsignada = Transformations.map(this.usuario) {
        if(it == null) "U0" else "U${it.idUnidad}"
    }
}