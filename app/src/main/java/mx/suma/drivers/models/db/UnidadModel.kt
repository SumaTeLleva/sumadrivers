package mx.suma.drivers.models.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "unidades")
data class UnidadModel (
    @PrimaryKey var id: Long = -1,
    var descripcion: String = "",
    var pasajeros: Int = -1,
    var numeroEconomico: Int = -1,
    var tipo: String = "",
    var fotografia: String = "",
    var gps: String? = null,
    var modelo: Int = -1,
    var isActiva: Boolean = false,
    var locatorWialon: String? = null,
    var itemIdWialon: Int = -1,
    var iconoUnidad: String = "",
    var placasFederales: String = ""
) : Parcelable