package com.tour.ar.model;

import android.location.Location;

/**
 * Created by ntdat on 1/16/17.
 */

public class ARPoint {
    Location location;
    String name;
    double lat;
    double lon;
    public ARPoint(String name, double lat, double lon, double altitude) {
        this.name = name;
        location = new Location("ARPoint");
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(altitude);
        this.lat=lat;
        this.lon=lon;

    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
    public double getLat(){return lat; }
    public double getLon(){return lon; }

}
