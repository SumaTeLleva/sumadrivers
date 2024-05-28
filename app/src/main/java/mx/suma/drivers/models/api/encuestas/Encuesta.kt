package mx.suma.drivers.models.api.encuestas

class Encuesta {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = data.attributes

    class Data {
        var id: Long = -1L
        var attributes = Attributes()

        class Attributes {
            var nombre: String = ""
            var proposito: String = ""
            var fechaInicial: String = ""
            var fechaFinal: String = ""
            var frecuencia: Long = -1
            var ponderada: Boolean = false
        }
    }
}