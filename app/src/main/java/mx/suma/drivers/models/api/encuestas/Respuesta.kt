package mx.suma.drivers.models.api.encuestas

import java.io.File

class Respuesta {
    var data: Data = Data()

    val attr: Data.Attributes
        get() = data.attributes

    class Data {
        var id: Long = -1L
        var attributes = Attributes()

        class Attributes {
            var usuarioId: Long = -1
            var encuestaId: Long = -1
            var preguntaId: Long = -1
            var contestadaAt: String? = null
            var respuesta: Boolean = false
        }
    }
}

class RespuestaUnidad {
    var idEmpleado: Int = -1
    var idUnidad: Int = -1
    var listRespuesta: ArrayList<Respuesta> = ArrayList()

    class Respuesta {
        var idPregunta: Int = -1
        var idRespuesta: Int = -1
        var idParent: Int = -1
        constructor(idPregunta:Int, idRespuesta: Int, idParent: Int) {
            this.idPregunta = idPregunta
            this.idRespuesta = idRespuesta
            this.idParent = idParent
        }

    }

    class RespuestaImagen {
        var idPregunta: Int = -1
        var image: File? = null

        constructor(idPregunta:Int, image:File){
            this.idPregunta = idPregunta
            this.image = image
        }
    }
}