package mx.suma.drivers.models.api.usuarios

import mx.suma.drivers.models.api.Operador

class Usuario {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = this.data.attributes

    val rel: Data.Relations
        get() = this.data.relations

    class Data {
        var type: String = ""
        var id: Long = -1
        var attributes: Attributes = Attributes()
        var relations: Relations = Relations()

        class Attributes {
            var nombre: String = ""
            var email: String = ""
            var acceso: Boolean = false
            var activo: Boolean = false
            var supervisor: Boolean = false
            var idOperadorSuma: Long = -1
            var idEmpresa: Long = -1
            var idProveedor: Long = -1
            var numeroTelefono: String = ""
        }

        class Relations {
            var tipo: Tipo = Tipo()
            var operadorSuma: Operador =
                Operador()
        }
    }
}

