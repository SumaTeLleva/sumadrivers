package mx.suma.drivers.models.api

class Operador {
    var data: Data = Data()
    val attr: Data.Attributes
        get() = this.data.attributes

    class Data {
        var type: String = "operador_suma"
        var id: Long = -1
        var attributes: Attributes = Attributes()

        class Attributes {
            var nombre: String = "Sin definir"
            var email: String = "Sin definir"
            var numeroTelefono: String = "Sin definir"
            var fotografia: String = "Sin definir"
            var esSubcontratado: Boolean = false
            var activo: Boolean = false
            var fechaActualizacionUnidad: String? = null
            var idUnidad: Long = -1
            var idProveedor: Long = -1
        }
    }
}