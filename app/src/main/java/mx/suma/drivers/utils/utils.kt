package mx.suma.drivers.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.LongSparseArray
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.suma.drivers.R
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import timber.log.Timber
import java.math.BigInteger
import java.security.MessageDigest
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
val DATE_FORMAT = "yyyy-MM-dd"
val TIME_FORMAT_24H = "HH:mm"
val TIME_FORMAT_AMPM = "h:mm a"
val MONTH_YEAR_FORMAT = "MMMM yyyy"
val FRIENDLY_DATE_FORMAT = "dd-MMMM-yyyy"

fun now(): Calendar {
    return Calendar.getInstance()
}

fun Calendar.startOfDay(): Calendar {
    val newTime = Calendar.getInstance()
    newTime.time = this.time
    newTime.set(Calendar.HOUR_OF_DAY, 0)
    newTime.set(Calendar.MINUTE, 0)
    newTime.set(Calendar.SECOND, 0)
    newTime.set(Calendar.MILLISECOND, 0)
    return newTime
}


fun Calendar.endOfDay(): Calendar {
    val newTime = Calendar.getInstance()
    newTime.time = this.time
    newTime.set(Calendar.HOUR_OF_DAY, 23)
    newTime.set(Calendar.MINUTE, 59)
    newTime.set(Calendar.SECOND, 59)
    newTime.set(Calendar.MILLISECOND, 999)
    return newTime
}

fun Calendar.endOfMonth(): Calendar {
    val newTime = Calendar.getInstance()
    newTime.time = this.endOfDay().time

    val actual = this.getActualMaximum(Calendar.DAY_OF_MONTH)
    newTime.set(Calendar.DAY_OF_MONTH, actual)

    Timber.d(newTime.time.formatAsDateTimeString())

    return newTime
}

fun Calendar.startOfYear(): Calendar {
    val newTime = startOfDay()
    newTime.time = this.time
    newTime.set(Calendar.MONTH, 0)
    newTime.set(Calendar.DATE, 1)
    return newTime
}

fun Calendar.endOfYear(): Calendar {
    val newTime = startOfDay()
    newTime.time = this.time
    newTime.set(Calendar.MONTH, 11)
    newTime.set(Calendar.DATE, 31)
    return newTime
}


fun Calendar.startOfMonth(): Calendar {
    val newTime = Calendar.getInstance()
    newTime.time = this.startOfDay().time

    val actual = this.getActualMinimum(Calendar.DAY_OF_MONTH)
    newTime.set(Calendar.DAY_OF_MONTH, actual)

    Timber.d(newTime.time.formatAsDateTimeString())

    return newTime
}

fun Calendar.nextMonth(): Calendar {
    val newTime = Calendar.getInstance()
    newTime.time = this.endOfMonth().time
    newTime.add(Calendar.HOUR_OF_DAY, 24)

    Timber.d(newTime.time.formatAsDateTimeString())

    return newTime
}

fun Calendar.previousMonth(): Calendar {
    val newTime = Calendar.getInstance()
    newTime.time = this.startOfMonth().time
    newTime.add(Calendar.HOUR_OF_DAY, -24)

    Timber.d(newTime.time.formatAsDateTimeString())

    return newTime
}

fun convertStringToCalendar(time:String): Calendar {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat(DATE_TIME_FORMAT)
    return try {
        val date:Date = dateFormat.parse(time)
        calendar.time = date
        calendar
    }catch (e: ParseException) {
        calendar
    }
}

fun Calendar.getCurrentHour(): Int {
    return this.get(Calendar.HOUR_OF_DAY)
}

fun getDefaultLocale(): Locale {
    return Locale("es_MX")
}

fun getDateTimeFormatter(format: String = DATE_TIME_FORMAT): SimpleDateFormat {
    return SimpleDateFormat(format, getDefaultLocale())
}

fun parseDateTimeString(dateString: String): Calendar {
    val date = getDateTimeFormatter().parse(dateString)

    val tiempo = Calendar.getInstance()
    tiempo.time = date as Date

    return tiempo
}

fun millisToHours(milliseconds: Long): Long {
    return milliseconds / (1000 * 60 * 60)
}

fun Date.formatAsDateTimeString(): String {
    return getDateTimeFormatter().format(this)
}

fun Date.formatDateAsDateString(): String {
    return getDateTimeFormatter(DATE_FORMAT).format(this)
}

fun Date.formatDateAsTimeAmPmString(): String {
    return getDateTimeFormatter(TIME_FORMAT_AMPM).format(this)
}

fun Date.formatDateAsTime24HString(): String {
    return getDateTimeFormatter(TIME_FORMAT_24H).format(this)
}

fun Date.formatDateAsMonthYearString(): String {
    return getDateTimeFormatter(MONTH_YEAR_FORMAT).format(this)
}

fun Date.formatDateAsFriendlyFormat(): String {
    return getDateTimeFormatter(FRIENDLY_DATE_FORMAT).format(this)
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

object LogosClientes {
    private val logos = LongSparseArray<Int>()

    init {
        logos.put(121L, R.drawable.logo_adecco)
        logos.put(132L, R.drawable.logo_alorica)
        logos.put(95L, R.drawable.logo_apymsa)
        logos.put(105L, R.drawable.logo_castor)
        logos.put(86L, R.drawable.logo_cjf)
        logos.put(103L, R.drawable.logo_eaton)
        logos.put(130L, R.drawable.logo_efi)
        logos.put(13L, R.drawable.logo_ematec)
        logos.put(122L, R.drawable.logo_extrupac)
        logos.put(134L, R.drawable.logo_fmc)
        logos.put(106L, R.drawable.logo_global_gas)
        logos.put(119L, R.drawable.logo_apro)
        logos.put(94L, R.drawable.logo_heat_control)
        logos.put(59L, R.drawable.logo_hpi_fuentes)
        logos.put(100L, R.drawable.logo_intel)
        logos.put(125L, R.drawable.logo_kn)
        logos.put(126L, R.drawable.logo_logis)
        logos.put(99L, R.drawable.logo_maver)
        logos.put(67L, R.drawable.logo_monsanto)
        logos.put(131L, R.drawable.logo_dico)
        logos.put(120L, R.drawable.logo_nattura)
        logos.put(96L, R.drawable.logo_odem)
        logos.put(72L, R.drawable.logo_oxiteno)
        logos.put(127L, R.drawable.logo_oxxo)
        logos.put(110L, R.drawable.logo_cjf)
        logos.put(123L, R.drawable.logo_seerauber)
        logos.put(135L, R.drawable.logo_sigma)
        logos.put(115L, R.drawable.logo_sigma)
        logos.put(101L, R.drawable.logo_sumida)
        logos.put(90L, R.drawable.logo_urrea)
        logos.put(89L, R.drawable.logo_zf)
        logos.put(76L, R.drawable.logo_zimag)
        logos.put(-1L, R.drawable.logo_suma)
        logos.put(5L, R.drawable.logo_suma)
    }

    fun getLogo(idCliente: Long): Int {
        return logos.get(idCliente) ?: logos.get(5L)
    }
}

fun getLogo(idCliente: Long): Int {
    val logos = LongSparseArray<Int>()
    logos.put(121L, R.drawable.logo_adecco)
    logos.put(132L, R.drawable.logo_alorica)
    logos.put(95L, R.drawable.logo_apymsa)
    logos.put(105L, R.drawable.logo_castor)
    logos.put(86L, R.drawable.logo_cjf)
    logos.put(103L, R.drawable.logo_eaton)
    logos.put(130L, R.drawable.logo_efi)
    logos.put(13L, R.drawable.logo_ematec)
    logos.put(122L, R.drawable.logo_extrupac)
    logos.put(134L, R.drawable.logo_fmc)
    logos.put(106L, R.drawable.logo_global_gas)
    logos.put(119L, R.drawable.logo_apro)
    logos.put(94L, R.drawable.logo_heat_control)
    logos.put(59L, R.drawable.logo_hpi_fuentes)
    logos.put(100L, R.drawable.logo_intel)
    logos.put(125L, R.drawable.logo_kn)
    logos.put(126L, R.drawable.logo_logis)
    logos.put(99L, R.drawable.logo_maver)
    logos.put(67L, R.drawable.logo_monsanto)
    logos.put(131L, R.drawable.logo_dico)
    logos.put(120L, R.drawable.logo_nattura)
    logos.put(96L, R.drawable.logo_odem)
    logos.put(72L, R.drawable.logo_oxiteno)
    logos.put(127L, R.drawable.logo_oxxo)
    logos.put(110L, R.drawable.logo_cjf)
    logos.put(123L, R.drawable.logo_seerauber)
    logos.put(135L, R.drawable.logo_sigma)
    logos.put(115L, R.drawable.logo_sigma)
    logos.put(101L, R.drawable.logo_sumida)
    logos.put(90L, R.drawable.logo_urrea)
    logos.put(89L, R.drawable.logo_zf)
    logos.put(76L, R.drawable.logo_zimag)
    logos.put(-1L, R.drawable.logo_suma)
    logos.put(5L, R.drawable.logo_suma)

    return logos.get(idCliente) ?: logos.get(5L)
}

fun getRequestBodyFromPayload(payload: JSONObject): RequestBody {
    return RequestBody.create(
        MediaType.get("application/json"), payload.toString()
    )
}

suspend fun simulateLatency(millis: Long = 3000) {
    withContext(Dispatchers.IO) {
        Thread.sleep(millis)
    }
}

fun BitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    val vectorDrawable = ContextCompat.getDrawable(
        context, vectorResId
    )
    vectorDrawable!!.setBounds(
        0, 0, vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight
    )
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}