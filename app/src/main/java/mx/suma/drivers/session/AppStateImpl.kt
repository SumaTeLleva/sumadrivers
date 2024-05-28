package mx.suma.drivers.session

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

@SuppressLint("CommitPrefEdits")
class AppStateImpl(val context: Context):
        Preferences, AppState{

    private val SETTINGS_NAME = "app_state"

    private var mPreferences: SharedPreferences
            = PreferenceManager.getDefaultSharedPreferences(context)
    private var mEditor: SharedPreferences.Editor

    init {
        mEditor = mPreferences.edit()
    }

    override fun getPreferences(): SharedPreferences = mPreferences

    override fun getEditor(): SharedPreferences.Editor = mEditor

    override fun shouldBulkUpdate(): Boolean = false

    override fun settingsName(): String = SETTINGS_NAME

    override fun setFirebaseToken(token: String) {
        put(PreferenceKey.HAS_FIREBASE_TOKEN, true)
        put(PreferenceKey.FIREBASE_TOKEN_STR, token)
    }

    override fun getFirebaseToken(): String {
        return getString(PreferenceKey.FIREBASE_TOKEN_STR, "")
    }

    override fun setDispositivoId(id: Long) {
        put(PreferenceKey.IS_DISPOSITIVO_SUSCRIBED, true)
        put(PreferenceKey.DISPOSITIVO_ID_LONG, id)
    }

    override fun getDispositivoId(): Long {
        return getLong(PreferenceKey.DISPOSITIVO_ID_LONG, -1)
    }

    override fun hasDirectorio(): Boolean {
        return getBoolean(PreferenceKey.HAS_CACHE_DIRECTORIO, false)
    }

    override fun marcarCacheDirectorio() {
        put(PreferenceKey.HAS_CACHE_DIRECTORIO, true)
    }

    override fun invalidarCacheDirectorio() {
        put(PreferenceKey.HAS_CACHE_DIRECTORIO, false)
    }

    override fun marcarAutenticado() {
        put(PreferenceKey.IS_LOGGED_IN, true)
    }

    override fun autenticado(): Boolean {
        return getBoolean(PreferenceKey.IS_LOGGED_IN, false)
    }

    override fun hasCacheUnidades(): Boolean {
        return getBoolean(PreferenceKey.HAS_CACHE_UNIDADES, false)
    }

    override fun marcarCacheUnidades() {
        put(PreferenceKey.HAS_CACHE_UNIDADES, true)
    }

    override fun invalidarCacheUnidades() {
        put(PreferenceKey.HAS_CACHE_UNIDADES, false)
    }

    override fun hasCacheGasolineras(): Boolean {
        return getBoolean(PreferenceKey.HAS_CACHE_GASOLINERAS, false)
    }

    override fun marcarCacheGasolineras() {
        put(PreferenceKey.HAS_CACHE_GASOLINERAS, true)
    }

    override fun invalidarCacheGasolineras() {
        put(PreferenceKey.HAS_CACHE_GASOLINERAS, false)
    }

    override fun setLastKnownLocation(latitud: Double, longitud: Double, time: Double) {
        put(PreferenceKey.LATITUD_DBL, latitud)
        put(PreferenceKey.LONGITUD_DBL, longitud)
        put(PreferenceKey.LOCATION_TIME_DBL, time)
    }

    override fun getLastKnownLocation(): HashMap<String, Double> {
        val location = HashMap<String, Double>()

        val latitud = getDouble(PreferenceKey.LATITUD_DBL, 0.toDouble())
        val longitud = getDouble(PreferenceKey.LONGITUD_DBL, 0.toDouble())
        val time = getDouble(PreferenceKey.LOCATION_TIME_DBL, 0.toDouble())

        location.put("latitud", latitud)
        location.put("longitud", longitud)
        location.put("time", time)

        return location
    }

    override fun setUltimoKilometraje(kilometraje: Long) {
        put(PreferenceKey.ULTIMO_KILOMETRAJE_LONG, kilometraje)
    }

    override fun getUltimoKilometraje(): Long {
        return getLong(PreferenceKey.ULTIMO_KILOMETRAJE_LONG, 0)
    }

    override fun setUltimoFolioBitacora(folio: Long) {
        put(PreferenceKey.ULTIMO_FOLIO_BITACORA_LONG, folio)
    }

    override fun getUltimoFolioBitacora(): Long {
        return getLong(PreferenceKey.ULTIMO_FOLIO_BITACORA_LONG, 0)
    }

    override fun setFechaUltimaEncuesta(aDate: String) {
        put(PreferenceKey.ULTIMA_ENCUESTA_STR, aDate)
    }

    override fun getFechaUltimaEncuesta(): String {
        return getString(PreferenceKey.ULTIMA_ENCUESTA_STR, "")
    }

    override fun invalidarSesion() = clear()

    override fun setIsProducction(flag:Boolean) {
        put(PreferenceKey.IS_PRODUCCTION,flag)
    }
    override fun getIsProducction():Boolean {
        return getBoolean(PreferenceKey.IS_PRODUCCTION, false)
    }

    override fun setDateSession(date: Long) {
        put(PreferenceKey.DATE_START_SESSION, date)
    }
    override fun getDateSession(): Long {
        return getLong(PreferenceKey.DATE_START_SESSION, 0)
    }

    override fun setLastTesting(date: Long, unitId: Int) {
        put(PreferenceKey.LASTDATE, date)
        put(PreferenceKey.LASTVEHICLE, unitId)
    }
    override fun getLastTesting(): HashMap<String, Any> {
        val response = HashMap<String, Any>()
        val date = getLong(PreferenceKey.LASTDATE, 0)
        val unitId = getInt(PreferenceKey.LASTVEHICLE, 0)

        response.put("lastDate", date)
        response.put("lastUnit", unitId)

        return response
    }
    override fun setUltimoKilometrajeInicial(kilometraje: Long) {
        put(PreferenceKey.ULTIMO_KILOMETRAJE_INICIAL_LONG, kilometraje)
    }
    override fun getUltimoKilometrajeInicial():Long {
        return getLong(PreferenceKey.ULTIMO_KILOMETRAJE_INICIAL_LONG, 0)
    }

    override fun setPathServer(path: String) {
        put(PreferenceKey.PATH_SERVER, path)
    }

    override fun getPathServer(): String {
        return getString(PreferenceKey.PATH_SERVER, "")
    }
}