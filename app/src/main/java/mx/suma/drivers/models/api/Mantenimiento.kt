package mx.suma.drivers.models.api

import mx.suma.drivers.models.api.usuarios.Usuario


class Mantenimiento {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = data.attributes

    val rel: Data.Relations
        get() = data.relations

    class Data {
        var type: String = "mantenimiento"
        var id: Long = -1
        var attributes: Attributes = Attributes()
        var relations: Relations = Relations()

        class Attributes {
            var fecha: String = ""
            var idUsuario: Long = -1
            var idUnidad: Long = -1
            var titulo: String = ""
            var notas: String = ""
            var solucion: String = ""
            var concluido: Boolean = false
        }

        class Relations {
            var usuario: Usuario? = null
            var unidad: Unidad? = null
        }
    }
}