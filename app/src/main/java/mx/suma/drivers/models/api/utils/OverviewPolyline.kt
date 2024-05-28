package mx.suma.drivers.models.api.utils

import com.google.gson.annotations.SerializedName

data class OverviewPolyline(
    @SerializedName("points")
    var points: String?
)
