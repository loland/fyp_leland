package com.example.mymap.Routes;

import com.example.mymap.General.Landmark;

import java.util.ArrayList;

public class BusRoute extends Route {
    public String busNumber;
    public String type = "BUS";
    public ArrayList<Landmark> stopList;

    public BusRoute(String from, String to, String polyline, String busNumber) {
        super(from, to, polyline);
        this.busNumber = busNumber;
    }

    public BusRoute(String from, String to, String polyline, String busNumber, ArrayList<Landmark> stopList) {
        super(from, to, polyline);
        this.busNumber = busNumber;
        this.stopList = stopList;
    }

    @Override
    public ArrayList<Landmark> getStopList() {
        return stopList;
    }

    @Override
    public String getRouteString() {
        return String.format("Take %s %s from %s to %s", this.type, this.busNumber, this.from, this.to);
    }

    @Override
    public String getAudioString() {
        return String.format("Take %s %s to %s", this.type, this.busNumber, this.to);
    }
}
