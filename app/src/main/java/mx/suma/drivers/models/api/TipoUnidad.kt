package mx.suma.drivers.models.api

class TipoUnidad {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = data.attributes

    class Data {
        var type: String = "tipo_unidad"
        var id: Long = -1
        var attributes: Attributes = Attributes()

        class Attributes {
            var etiquetaTipoUnidad: String = "Sin definir"
            var rendimientoPromedio: Double = 0.0
            var rendimientoMaximo: Double = 0.0
            var rendimientoMinimo: Double = 0.0
            var tipoCombustible: String = "Sin definir"
            var capacidadTanque: Double = 0.0
        }
    }
}