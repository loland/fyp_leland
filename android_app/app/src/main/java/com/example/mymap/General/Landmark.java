package com.example.mymap.General;

import com.example.mymap.Utility.LandmarkHandler;

import java.io.Serializable;

public class Landmark implements Serializable {
    public String coord;
    public String letter;
    public String color;
    public String type; // bus stop / traffic light
    public String name; // holds name of the bus stop/mrt station
    public Integer detectionDistance;

    public Landmark(String coord, String letter, String color, String type) {
        this(coord, letter, color, type, null);
    }

    public Landmark(String coord, String letter, String color, String type, String name) {
        this.coord = coord;
        this.letter = letter;
        this.color = color;
        this.type = type;
        this.name = name;

        if (type.equals(LandmarkHandler.BUS_STOP_TYPE)) {
            this.detectionDistance = 25;
        } else if (type.equals(LandmarkHandler.TRAFFIC_LIGHT_TYPE)) {
            this.detectionDistance = 10;
        } else if (type.equals(LandmarkHandler.NLB_LOGO_TYPE)) {
            this.detectionDistance = 30;
        } else {
            this.detectionDistance = 25;
        }
    }

    public Landmark(String coord, String letter, String color, String type, String name, int detectionDistance) {
        this.coord = coord;
        this.letter = letter;
        this.color = color;
        this.type = type;
        this.name = name;
        this.detectionDistance = detectionDistance;
    }
}
