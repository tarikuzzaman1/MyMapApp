package com.rosc.android.map.mymapapp.realm;

import io.realm.RealmObject;

/**
 * Created by TARIK-ROSC on 1/2/2018.
 */

public class LocationTask extends RealmObject {

    private double latitude;
    private double longitude;
    private String date;

    public LocationTask() {
    }

    public LocationTask(double latitude, double longitude, String date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
