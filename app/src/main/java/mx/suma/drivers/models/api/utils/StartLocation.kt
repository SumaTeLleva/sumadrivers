package mx.suma.drivers.models.api.utils

import com.google.gson.annotations.SerializedName

data class StartLocation(
    @SerializedName("lat")
    var lat: Double?,
    @SerializedName("lng")
    var lng: Double?
)
