package mx.suma.drivers.utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;

import mx.suma.drivers.models.api.EstacionGas;

public class SortEstacion implements Comparator<EstacionGas.Estacion> {
    LatLng currentLoc;

    public SortEstacion(LatLng current) {
        currentLoc = current;
    }

    @Override
    public int compare(final EstacionGas.Estacion estacion1, final EstacionGas.Estacion estacion2) {
        double lat1 = Double.parseDouble(estacion1.getLATITUD());
        double lon1 = Double.parseDouble(estacion1.getLONGITUD());
        double lat2 = Double.parseDouble(estacion2.getLATITUD());
        double lon2 = Double.parseDouble(estacion2.getLONGITUD());

        double distanceToPlace1 = distance(currentLoc.latitude, currentLoc.longitude, lat1, lon1);
        double distanceToPlace2 = distance(currentLoc.latitude, currentLoc.longitude, lat2, lon2);
        return (int) (distanceToPlace1 - distanceToPlace2);
    }

    public double distance(double fromLat, double fromLon, double toLat, double toLon) {
        double radius = 6378137;   // approximate Earth radius, *in meters*
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;
        double angle = 2 * Math.asin( Math.sqrt(
                Math.pow(Math.sin(deltaLat/2), 2) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                                Math.pow(Math.sin(deltaLon/2), 2) ) );
        return radius * angle;
    }
}
