package mx.suma.drivers.models.api

class Unidades {
    var data: ArrayList<Data> = ArrayList()

    class Data {
        var ID: Int = -1
    }
}
class Unidad {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = data.attributes

    val rel: Data.Relations
        get() = data.relations

    class Data {
        var type: String = "unidad"
        var id: Long = -1
        var attributes: Attributes = Attributes()
        var relations: Relations = Relations()

        class Attributes {
            var descripcion: String = ""
            var pasajeros: Int = -1
            var numeroEconomico: Int = -1
            var tipo: String = ""
            var fotografia: String = ""
            var gps: String? = null
            var modelo: Int = -1
            var isActiva: Boolean = false
            var locatorWialon: String? = null
            var itemIdWialon: Int = -1
            var iconoUnidad: String = ""
            var placasFederales: String = ""
            var lectorHuella: Boolean = false
            var kilometraje: Long = -1
        }

        class Relations {
            var operador: Operador = Operador()
            var tipoUnidad: TipoUnidad = TipoUnidad()
        }
    }
}

class KilometrajeUnidad {
    var attributes: Attributes = Attributes()
    class Attributes {
        var id: Int = -1
        var nombre: String = ""
        var ID_UNIDAD: Int = -1
        var kilometraje: Long = -1
        var DESCRIPCION: String = ""
        var RENDIMIENTO: Float = -1f
        var CAPACIDAD_TANQUE: Float = - 1f
        var RENDIMIENTO_CAPACIDAD: Float = 600f
    }

    fun getMaxKilometraje(km:Long): Float {
        return  km + this.attributes.RENDIMIENTO_CAPACIDAD
    }
}
class ResSaveNewOdometro {
    var success: Boolean = false
    var nuevo_valor: Int = -1
}