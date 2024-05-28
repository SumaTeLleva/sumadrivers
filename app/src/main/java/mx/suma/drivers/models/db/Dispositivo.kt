package mx.suma.drivers.models.db

import android.os.Build
import mx.suma.drivers.BuildConfig
import org.json.JSONObject

data class Dispositivo (
        val installationId: String,
        val imei: String = "No disponible",
        val usuario: Long,
        val versionCode: Int,
        val versionName: String,
        val brand: String,
        val fingerPrint: String,
        val manufacturer: String,
        val deviceModel: String,
        val serial: String = "No disponible",
        val activo: Boolean) {

    fun getPayload() : JSONObject {
        return JSONObject().put("installation_id", installationId)
                .put("usuario", usuario)
                .put("imei", imei)
                .put("version_code", versionCode)
                .put("version_name", versionName)
                .put("brand", brand)
                .put("fingerprint", fingerPrint)
                .put("manufacturer", manufacturer)
                .put("device_model", deviceModel)
                .put("serial", serial)
                .put("activo", activo)
    }

    companion object {
        fun createDispositivo(idUsuario: Long, firebaseInstanceId: String) : Dispositivo {
            return Dispositivo(
                installationId = firebaseInstanceId,
                imei = "No disponible",
                usuario = idUsuario,
                versionCode =  BuildConfig.VERSION_CODE,
                versionName = BuildConfig.VERSION_NAME,
                brand = Build.BRAND,
                fingerPrint = Build.FINGERPRINT,
                manufacturer = Build.MANUFACTURER,
                deviceModel = Build.MODEL,
                activo = true)
        }
    }
}