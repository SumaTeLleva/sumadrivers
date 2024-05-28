package mx.suma.drivers.models.api

class Sanitizacion {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = this.data.attributes

    val rel: Data.Relations
        get() = this.data.relations

    class Data {
        var id: Long = -1
        var attributes: Attributes =
            Attributes()
        var relations: Relations = Relations()

        class Attributes {
            var fecha: String? = null
            var tiempoInicial: String? = null
            var tiempoFinal: String? = null
            var coordenadas: List<Double> = listOf()
            var clienteId: Long = -1
            var operadorId: Long = -1
            var unidadId: Long = -1
        }

        class Relations {
            var cliente: Cliente = Cliente()
            var operador: Operador = Operador()
            var unidad: Unidad = Unidad()
        }
    }
}