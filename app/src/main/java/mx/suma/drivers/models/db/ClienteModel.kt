package mx.suma.drivers.models.db

data class ClienteModel(
    var id: Long = -1,
    var nombreEmpresa: String = "",
    var logoEmpresa: String = "",
    var activo: Boolean = false,
    var requiereCentroCostos: Boolean = false,
    var politicaTaxis: String = "",
    var politicaServicio: String = "",
    var mostrarTelefonoOperador: Boolean = false,
    var ubicacionEmpresa: List<Double> = listOf(),
    var idCategoria: Int = -1,
    var idCoordinadorServicio: Int = -1,
    var idSupervisorServicio: Int = -1,
    var idJefeOperaciones: Int = -1,
    var iniciales: String = "",
    var nipMapas: Int = -1,
    var idUsuarioCoordinador: Int = -1
)