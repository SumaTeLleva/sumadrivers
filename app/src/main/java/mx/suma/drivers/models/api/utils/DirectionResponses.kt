package mx.suma.drivers.models.api.utils

import com.google.gson.annotations.SerializedName

data class DirectionResponses(
    @SerializedName("geocoded_waypoints")
    var geocodedWaypoints: List<GeocodedWaypoint?>?,
    @SerializedName("routes")
    var routes: List<Route?>?,
    @SerializedName("status")
    var status: String?,
    @SerializedName("error_message")
    var error_message: String? = ""
)
