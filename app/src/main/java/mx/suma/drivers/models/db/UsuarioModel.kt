package mx.suma.drivers.models.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "usuarios")
data class UsuarioModel(
    @PrimaryKey var id: Long = 0L,
    var nombre: String = "",
    var email: String = "",
    var acceso: Boolean = false,
    var activo: Boolean = false,
    var supervisor: Boolean = false,
    var idOperadorSuma: Long = 0L,
    var idEmpresa: Long = 0L,
    var idProveedor: Long = 0L,
    var numeroTelefono: String = "",
    var fotografia: String = "",
    var idUnidad: Long = -1
) : Parcelable