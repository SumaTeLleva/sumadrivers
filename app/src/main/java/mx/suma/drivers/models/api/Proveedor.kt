package mx.suma.drivers.models.api


class Proveedor {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = data.attributes

    class Data {
        var type: String = "proveedor"
        var id: Long = -1
        var attributes: Attributes = Attributes()

        class Attributes {
            var nombre: String = "Sin definir"
            var email: String = "Sin definir"
            var numeroTelefono: String = "Sin definir"
            var tipoContacto: String = "Sin definir"
            var idCategoria: Int = -1
            var isActivo: Boolean = false
        }
    }
}