package mx.suma.drivers.models.api

class Destino {
    val data: ArrayList<Lugar> = ArrayList()

    class Lugar {
        val ID: Int = -1
        val ID_PROVEEDOR: Int = -1
        val DOMICILIO: String = ""
        val COLONIA: String = ""
        val CP: Long = -1L
        val ID_CO_CIUDAD: Int = -1
        val ID_B_ESTADO: Int = -1
        val LATITUD: String = ""
        val LONGITUD: String = ""
        val ACTIVO: Int = -1
    }
}