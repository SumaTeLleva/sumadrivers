package mx.suma.drivers.models.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proveedores")
data class ProveedorModel(
    @PrimaryKey var id: Long = -1,
    var nombre: String = "Sin definir",
    var email: String = "Sin definir",
    var numeroTelefono: String = "Sin definir",
    var tipoContacto: String = "Sin definir",
    var idCategoria: Int = -1,
    var isActivo: Boolean = false
)
