package mx.suma.drivers.models.db

import java.io.File


data class ImagenModel(
    var id_pregunta: Int,
    var imagen: File,
    var id_encuesta: Int,
)
