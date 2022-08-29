package com.example.mymap.Routes;

public class WalkRoute extends Route {
    public int distance;
    public String type = "WALK";

    public WalkRoute(String from, String to, String polyline, int distance) {
        super(from, to, polyline);
        this.distance = distance;
    }
    @Override
    public String getRouteString() {
        return String.format("%s from %s to %s (%sm)", this.type, this.from, this.to, this.distance);
    }

    @Override
    public String getAudioString() {
        return String.format("%s %s metres", this.type, this.distance);
    }
}
