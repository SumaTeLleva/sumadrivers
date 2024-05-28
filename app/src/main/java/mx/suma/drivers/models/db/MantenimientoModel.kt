package mx.suma.drivers.models.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
data class MantenimientoModel(
    @PrimaryKey var id: Long = -1L,
    var fecha: String = "",
    var idUsuario: Long = -1,
    var idUnidad: Long = -1,
    var titulo: String = "",
    var notas: String = "",
    var solucion: String = "",
    var concluido: Boolean = false
)

fun MantenimientoModel.getPayload(): JSONObject {
    val payload = JSONObject()
    payload.put("fecha", fecha)
    payload.put("usuario", idUsuario)
    payload.put("unidad", idUnidad)
    payload.put("titulo", titulo)
    payload.put("notas", notas)
    payload.put("concluido", concluido)

    return payload
}