package com.example.mymap.Routes;

import com.example.mymap.General.Landmark;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Route implements Serializable {
    public String from;
    public String to;
    public String polyline;

    public Route(String from, String to, String polyline) {
        this.from = from;
        this.to = to;
        this.polyline = polyline;
    }

    public String getRouteString() {
        return null;
    }

    public String getAudioString() {
        return null;
    }

    public ArrayList<Landmark> getStopList() {
        return null;
    }
}

// bus, bus number, from, to, polyline
// mrt, line, from, to, polyline
// walk, from, to, distance, polyline