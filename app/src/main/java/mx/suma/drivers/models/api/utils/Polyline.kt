package mx.suma.drivers.models.api.utils

import com.google.gson.annotations.SerializedName

data class Polyline(
    @SerializedName("points")
    var points: String?
)
