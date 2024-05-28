package mx.suma.drivers.models.db

data class PreguntaModel(
    var id: Long,
    var pregunta: String,
    var puntaje: Long,
    var tipo: Long,
    var imagenUrl: String,
    var encuestaId: Long
) {
    fun esPreguntaNoValida(): Boolean {
        return this.id == -1L
    }
}

fun makePreguntaInvalida(): PreguntaModel {
    return PreguntaModel(
        id = -1,
        pregunta = "Inválida",
        puntaje = 0,
        tipo = 0,
        imagenUrl = "Invalid",
        encuestaId = -1
    )
}