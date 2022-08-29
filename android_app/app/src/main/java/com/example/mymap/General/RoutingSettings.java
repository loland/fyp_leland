package com.example.mymap.General;

import android.widget.TextView;

public class RoutingSettings {
    public boolean manual;
    public boolean audio;
    public boolean autoCamera;
    public double lat;
    public double lon;
    public double INCREMENT = 0.00005;
    public int UPDATE_MAP_FREQUENCY = 8;

    public RoutingSettings() {
        this.lat = 0;
        this.lon = 0;
        this.manual = false;
        this.audio = true;
        this.autoCamera = true;
    }

    public void incLat() {
        this.lat += INCREMENT;
    }

    public void decLat() {
        this.lat -= INCREMENT;
    }

    public void incLon() {
        this.lon += INCREMENT;
    }

    public void decLon() {
        this.lon -= INCREMENT;
    }

    public void setCoords(String coords) {
        String[] coordList = coords.split(",", 2);
        this.lat = Double.parseDouble(coordList[0]);
        this.lon = Double.parseDouble(coordList[1]);
    }

    public String getCoords() {
        return getLat() + "," + getLon();
    }

    public String getLat() {
        return Double.toString(this.lat).substring(0, 7);
    }

    public String getLon() {
        return Double.toString(this.lon).substring(0, 9);
    }
}
