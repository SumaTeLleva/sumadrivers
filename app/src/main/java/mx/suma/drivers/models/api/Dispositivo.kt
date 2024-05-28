package mx.suma.drivers.models.api

class Dispositivo {

    var data: Data = Data()

    val attr: Data.Attributes
        get() = data.attributes

    class Data {
        var id: Long = -1L
        var attributes = Attributes()

        class Attributes {
            var installationId: String = ""
            var idUsuario: Long = -1L
            var activo: Boolean = false
        }
    }
}