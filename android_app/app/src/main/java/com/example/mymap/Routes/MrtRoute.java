package com.example.mymap.Routes;

import com.example.mymap.General.Landmark;

import java.util.ArrayList;

public class MrtRoute extends Route {
    public String line;
    public String type = "MRT";
    public ArrayList<Landmark> stopList;

    public MrtRoute(String from, String to, String polyline, String line) {
        super(from, to, polyline);
        this.line = line;
    }

    public MrtRoute(String from, String to, String polyline, String line, ArrayList<Landmark> stopList) {
        super(from, to, polyline);
        this.line = line;
        this.stopList = stopList;
    }

    @Override
    public ArrayList<Landmark> getStopList() {
        return stopList;
    }

    @Override
    public String getRouteString() {
        return String.format("Take %s (%s) from %s to %s", this.type, this.line, this.from, this.to);
    }

    @Override
    public String getAudioString() {
        return String.format("Take %s to %s", this.type, this.to);
    }
}
