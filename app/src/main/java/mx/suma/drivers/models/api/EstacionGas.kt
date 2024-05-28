package mx.suma.drivers.models.api

import com.google.android.gms.maps.model.LatLng
import mx.suma.drivers.utils.SortEstacion
import java.util.Collections


class EstacionGas {
    var data: ArrayList<Estacion> = ArrayList()
    class Estacion {
        var ID: Int = -1
        var ID_PROVEEDOR: Int = -1
        var NOMBRE: String = ""
        var DOMICILIO: String = ""
        var COLONIA: String = ""
        var CP: Int = -1
        var ID_CO_CIUDAD: Int = -1
        var ID_B_ESTADO: Int = -1
        var LATITUD: String = ""
        var LONGITUD: String = ""
        var ACTIVO: Int = -1
    }

    fun sortByDistance(currency: LatLng): ArrayList<Estacion> {
        Collections.sort(this.data, SortEstacion(currency))
        return this.data
    }
}