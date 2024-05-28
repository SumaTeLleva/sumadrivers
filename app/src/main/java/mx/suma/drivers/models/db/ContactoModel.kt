package mx.suma.drivers.models.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "directorio")
data class ContactoModel(
    @PrimaryKey
    var id: String = "",
    var nombre: String = "",
    var email: String = "",
    var telefono: String = "",
    var tipo: String = ""
)