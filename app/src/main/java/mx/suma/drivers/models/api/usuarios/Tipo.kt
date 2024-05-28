package mx.suma.drivers.models.api.usuarios


class Tipo {
    var data = Data()

    val attr: Data.Attributes
        get() = data.attributes

    class Data {
        var type = "tipo_usuario"
        var id = -1
        var attributes: Attributes = Attributes()

        class Attributes {
            var descripcion: String = "Sin definir"
        }
    }
}