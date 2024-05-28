package mx.suma.drivers.models.api


class Mapa {
    var data: Data = Data()

    val trazo get() = data.attributes.trazo
    val nombreMapa get() = data.attributes.nombreMapa

    class Data {
        var type: String = "mapa"
        var id: Long = -1
        var attributes: Attributes = Attributes()

        class Attributes {
            var trazo: String = ""
            var fechaCreacion: String = ""
            var fechaActualizacion: String = ""
            var nombreMapa: String = ""
            var tipo: String = ""
            var idMapa: Int = -1
            var idCliente: Int = -1
            var isEsMapaMaestro: Boolean = false
        }
    }
}