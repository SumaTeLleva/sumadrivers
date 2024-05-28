package mx.suma.drivers.models.api.encuestas

class Pregunta {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = data.attributes

    class Data {
        var id: Long = -1L
        var attributes = Attributes()

        class Attributes {
            var pregunta: String = ""
            var puntaje: Long = -1
            var tipo: Long = -1
            var imagenUrl: String = ""
            var encuestaId: Long = -1
        }
    }
}