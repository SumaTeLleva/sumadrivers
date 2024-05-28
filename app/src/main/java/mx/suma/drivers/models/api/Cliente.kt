package mx.suma.drivers.models.api

class Cliente {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = this.data.attributes

    class Data {
        var id: Long = -1
        var attributes: Attributes =
            Attributes()

        class Attributes {
            var nombreEmpresa = ""
            var logoEmpresa = ""
            var activo = false
            var requiereCentroCostos = false
            var politicaTaxis = ""
            var politicaServicio = ""
            var mostrarTelefonoOperador = false
            var ubicacionEmpresa = listOf<Double>()
            var idCategoria = -1
            var idCoordinadorServicio = -1
            var idSupervisorServicio = -1
            var idJefeOperaciones = -1
            var iniciales = ""
            var nipMapas = -1
            var idUsuarioCoordinador = -1
        }
    }
}