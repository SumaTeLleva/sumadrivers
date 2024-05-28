package mx.suma.drivers.models.db

import com.google.android.gms.maps.model.LatLng

data class LocationGasStation(
    var pointA: LatLng,
    var pointB: LatLng
)
