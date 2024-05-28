package mx.suma.drivers.models.db

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SanitizacionModel(
    var id: Long = -1,
    var fecha: String? = null,
    var tiempoInicial: String? = null,
    var tiempoFinal: String? = null,
    var coordenadas: List<Double> = listOf(),
    var clienteId: Long = -1,
    var operadorId: Long = -1,
    var unidadId: Long = -1,
    val nombreCliente: String = ""
) : Parcelable