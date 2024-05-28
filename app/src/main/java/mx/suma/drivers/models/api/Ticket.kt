 package mx.suma.drivers.models.api

class Ticket {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = data.attributes

    val rel: Data.Relations
        get() = data.relations

    class Data {
        var type: String = "ticket"
        var id: Long = -1
        var attributes: Attributes = Attributes()
        var relations: Relations = Relations()

        class Attributes {
            var fecha: String = ""
            var tipoCombustible: String = ""
            var folio: String = ""
            var monto: Double = 0.toDouble()
            var litros: Double = 0.toDouble()
            var precioCombustible: Double = 0.toDouble()
            var kilometraje: Long = 0
            var viaCaptura: Int = 1
            var idUnidad: Long = -1
            var idOperador: Long = -1
            var idGasolinera: Long = -1
        }

        class Relations {
            var unidad: Unidad = Unidad()
            var operador: Operador = Operador()
            var gasolinera: Proveedor = Proveedor()
        }
    }
}