package mx.suma.drivers.models.api.encuestas

import java.io.File

class EncuestaUnidad {
    var data: ArrayList<Modulo> = ArrayList()

    class Modulo {
        var MODULO: String = ""
        var PREGUNTA: ArrayList<Pregunta> = ArrayList()
    }

    class Pregunta {
        var ID_PREGUNTA:Int = -1
        var PREGUNTA:String = ""
        var OPCIONES: ArrayList<Option> = ArrayList()
    }

    class Option {
        var ID_RESPUESTA:Int = -1
        var RESPUESTA: String = ""
        var REQUIERE_FOTO: Boolean = false
        var image:File? = null
        var PREGUNTA: ArrayList<Pregunta> = ArrayList()
    }
}