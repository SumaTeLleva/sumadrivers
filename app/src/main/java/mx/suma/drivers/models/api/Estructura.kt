package mx.suma.drivers.models.api

class Estructura {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = this.data.attributes

    class Data {
        var type: String = "estructura"
        var id: Long = -1
        var attributes: Attributes = Attributes()

        class Attributes {
            var nombreRuta: String = "Sin definir"
            var turno: String = "Sin definir"
            var tipo: String? = null
            var tipoUnidad: String? = null
            var horaInicio: String? = null
            var horaFin: String? = null
            var fechaCreacion: String? = null
            var idMapa: Int = -1
            var idRuta: Int = -1
            var pagoOperador: Double = 0.toDouble()
            var isActiva: Boolean = false
        }
    }
}