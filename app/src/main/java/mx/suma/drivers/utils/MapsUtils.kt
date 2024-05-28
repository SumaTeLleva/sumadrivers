package mx.suma.drivers.utils

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import mx.suma.drivers.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InvalidObjectException
import java.util.*
import kotlin.collections.ArrayList

val getFeatures = { x:JSONObject ->
    val jsonArrayFeatures = x.getJSONArray("features")
    val arrayList = arrayListOf<JSONObject>()

    for(i in 0 until jsonArrayFeatures.length()) {
        if(jsonArrayFeatures[i] is JSONObject){
            arrayList.add(jsonArrayFeatures[i] as JSONObject)
        }
    }

    arrayList
}

val getFeatureGeometry = { x:JSONObject -> x.getJSONObject("geometry") }

val getFeatureProperties = { x:JSONObject -> x.getJSONObject("properties") }

fun getPropertie(properties: JSONObject, propertieName: String): String? {
    if(!properties.has(propertieName))
        return null

    return properties.getString(propertieName)
}

val position = { x:JSONObject ->
    val coordinates = getFeatureGeometry(x).getJSONArray("coordinates")
    LatLng(coordinates.getDouble(1), coordinates.getDouble(0))
}

val positions = { x:JSONObject ->
    val jsonArrayCoordinates = getFeatureGeometry(x).getJSONArray("coordinates")

    val coordinates = ArrayList<LatLng>()

    for(i in 0 until jsonArrayCoordinates.length()) {
        coordinates.add(
                LatLng((jsonArrayCoordinates[i] as JSONArray).getDouble(1),
                        (jsonArrayCoordinates[i] as JSONArray).getDouble(0))
        )
    }

    coordinates
}


val featureBounds = { xs: ArrayList<LatLng> ->
    val bounds = LatLngBounds.Builder()

    xs.forEach { bounds.include(it) }

    bounds
}

val getTitle = { x:JSONObject -> getFeatureProperties(x).getString("name") }

val getSnippet = { x:JSONObject ->

    val properties = getFeatureProperties(x)

    val builder = StringBuilder()
    builder.append("(")
    getPropertie(properties, "time1")?.let { builder.append("$it | ") }
    getPropertie(properties, "time2")?.let { builder.append("$it | ") }
    getPropertie(properties, "time3")?.let { builder.append(it) }
    builder.append(")")

    builder.toString()
}

val getFeatureType = { x:JSONObject -> getFeatureGeometry(x).getString("type") }

val marker = { x:JSONObject ->
    if(getFeatureType(x) != "Point")
        throw InvalidObjectException("Feature provided is not a Point!")

    MarkerOptions()
            .position(position(x))
            .title(getTitle(x))
            .snippet(getSnippet(x))
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_blue_busstop))
}
fun getPointCoord(x:JSONObject): LatLng {
    if(getFeatureType(x) != "Point")
        throw InvalidObjectException("Feature provided is not a Point!")
    val coord: LatLng = position(x)
    return coord
}

fun orderCoord(list: List<JSONObject>): List<JSONObject> {
    list.sortedWith { a,b ->
        try {
            var propA = a.getJSONObject("properties").get("time1").toString()
            var propB = b.getJSONObject("properties").get("time1").toString()
            if(propA > propB){
                -1
            }else {
                propA.compareTo(propB)
            }
        }catch (e: JSONException) {
            e.printStackTrace()
            -1
        }
    }
    return list
}

val polyline = { lineFeature: JSONObject ->
    if(getFeatureType(lineFeature) != "LineString")
        throw InvalidObjectException("Feature provided is not a Line!")

    val coordinates = positions(lineFeature)

    PolylineOptions()
            .addAll(coordinates)
            .color((0xFF1B5E20).toInt())
}
val getCoord = { xs: List<JSONObject> ->  xs.map{ getPointCoord(it) } }
val addMarkersToMap = { m:GoogleMap, xs: List<JSONObject> -> xs.forEach { m.addMarker(marker(it)) } }

val addPolylineToMap = { m:GoogleMap, l:PolylineOptions -> m.addPolyline(l) }

val getFeatureByType = { xs:List<JSONObject>, a:String -> xs.filter { getFeatureType(it) == a } }

val getPointFeatures = { xs:List<JSONObject> -> getFeatureByType(xs, "Point") }

val getLineStringFeatures = { xs:List<JSONObject> -> getFeatureByType(xs, "LineString") }