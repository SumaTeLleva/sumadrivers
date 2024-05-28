package mx.suma.drivers.models.api

class Bitacora {

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
            var modalidad: String = ""
            var terminado: Boolean = false
            var confirmado: Boolean = false
            var motivoTransferencia: String = ""
            var transferido: Boolean = false
            var transferidoAt: String? = null
            var tiempoInicial: String = ""
            var tiempoFinal: String = ""
            var fecha: String = ""
            var alarmaNotificacion: String = ""
            var alarmaInicioRuta: String = ""
            var comentarios: String? = null
            var estatus: Int = -1
            var folioBitacora: Long = -1
            var kilometrajeInicial: Long = -1
            var kilometrajeFinal: Long = -1
            var numeroPersonas: Int = -1
            var tiempoCantidad: String = ""
            var dia: Int = -1
            var semana: Int = -1
            var idProveedor: Long = -1
            var idOperador: Long = -1
            var idUnidad: Long = -1
            var idRuta: Long = -1
            var idEstructura: Long = -1
            var idServicioEspecial: Long = -1
            var horaConfirmacion: String? = null
            var horaBanderazo: String? = null
            var horaInicioRuta: String? = null
            var horaFinalRuta: String? = null
            var horaCierreRuta: String? = null
            var tipo: String = ""
            var verificado: Boolean = false
            var pagarServicio: Boolean = false
            var cancelado: Boolean = false
            var canceladoAt: String? = null
            var excepcion: Boolean = false
            var excepcionAt: String? = null
            var letreroEspecial: String = ""
        }

        class Relations {
            var ruta: Ruta = Ruta()
            var estructura: Estructura = Estructura()
            var unidad: Unidad = Unidad()
            var operador: Operador = Operador()
        }
    }
}

class BitacoraDate {
    var success: Boolean = false
    var data: Data = Data()

    class Data {
        var FECHA_HORA: String = ""
    }
}