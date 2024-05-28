package mx.suma.drivers.models.api

class Ruta {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = this.data.attributes

    class Data {
        var type: String = "ruta_empresarial"
        var id: Long = -1
        var attributes: Attributes = Attributes()
        var relations: Relations = Relations()

        class Attributes {
            var nombre: String = "Sin definir"
            var kilometros: Long = 0
            var tiempoEstimado: String = "Sin definir"
            var tipoServicio: Int = 0
            var horarioSalidas: String = "Sin definir"
            var idOperador: Long = -1
            var idMapa: Long = -1
            var idCliente: Long = -1
            var isActiva: Boolean = false
            var clienteRequiereAforo: Boolean = false
        }

        class Relations {
            /*var operador: Operador? = null
            var unidad: Unidad? = null
            var mapa: Mapa? = null*/
        }
    }
}