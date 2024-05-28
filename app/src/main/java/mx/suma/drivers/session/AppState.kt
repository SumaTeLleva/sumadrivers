package mx.suma.drivers.session

interface AppState {
    fun autenticado(): Boolean
    fun marcarAutenticado()
    fun invalidarSesion()

    fun hasDirectorio(): Boolean
    fun marcarCacheDirectorio()
    fun invalidarCacheDirectorio()

    fun hasCacheUnidades(): Boolean
    fun marcarCacheUnidades()
    fun invalidarCacheUnidades()

    fun hasCacheGasolineras(): Boolean
    fun marcarCacheGasolineras()
    fun invalidarCacheGasolineras()

    fun setFirebaseToken(token: String)
    fun getFirebaseToken(): String

    fun setDispositivoId(id: Long)
    fun getDispositivoId(): Long

    fun setLastKnownLocation(latitud: Double, longitud: Double, time: Double)
    fun getLastKnownLocation(): HashMap<String, Double>

    fun setUltimoFolioBitacora(folio: Long)
    fun getUltimoFolioBitacora(): Long

    fun setFechaUltimaEncuesta(aDate: String)
    fun getFechaUltimaEncuesta(): String

    fun setIsProducction(flag:Boolean)
    fun getIsProducction():Boolean

    fun setUltimoKilometraje(flag:Long)
    fun getUltimoKilometraje():Long

    fun setDateSession(date: Long)
    fun getDateSession(): Long

    fun setLastTesting(date: Long, unitId: Int)
    fun getLastTesting(): HashMap<String, Any>

    fun setUltimoKilometrajeInicial(kilometraje:Long)
    fun getUltimoKilometrajeInicial():Long

    fun setPathServer(path: String)
    fun getPathServer():String
}
