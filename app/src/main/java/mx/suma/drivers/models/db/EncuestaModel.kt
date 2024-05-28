package mx.suma.drivers.models.db

import java.util.*

data class EncuestaModel(
    val id: Long = -1,
    val nombre: String = "Invalid",
    val proposito: String = "Invalid",
    val fechaInicial: Calendar = Calendar.getInstance(),
    val fechaFinal: Calendar = Calendar.getInstance(),
    val frecuencia: Long = -1,
    val ponderada: Boolean = false
)