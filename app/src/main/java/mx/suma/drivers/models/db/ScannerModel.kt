package mx.suma.drivers.models.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "scanner")
data class ScannerModel (
    @PrimaryKey(autoGenerate = true) var id:Long = 0,
    var bitacoraId: Long = -1,
    var clienteId: Long = -1,
    var pasajeroId: Long = -1,
): Parcelable

fun ScannerModel.getPayload(): HashMap<String, String> {
    val parametros = HashMap<String, String>()
    parametros["id_cliente"] = "$clienteId"
    parametros["pasajero"] = "$pasajeroId"
    parametros["id_bitacora"] = "$bitacoraId"
    return parametros
}