package com.example.mymap.Routes;

import android.util.Log;

import com.example.mymap.General.Landmark;
import com.example.mymap.Routes.BusRoute;
import com.example.mymap.Routes.MrtRoute;
import com.example.mymap.Routes.Route;
import com.example.mymap.Routes.WalkRoute;
import com.example.mymap.Utility.LandmarkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Itinerary implements Serializable {
    public ArrayList<Route> routeList;

    public Itinerary(ArrayList<Route> routeList) {
        this.routeList = routeList;
    }

    public Route getRoute(int i) {
        if (i >= routeList.size()) {
            return null;
        }
        return routeList.get(i);
    }

    public int getRouteCount() {
        return this.routeList.size();
    }

    public String getRouteTypesString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < routeList.size(); i ++) {
            if (routeList.get(i) instanceof BusRoute) {
                sb.append("BUS");
            } else if (routeList.get(i) instanceof MrtRoute) {
                sb.append("MRT");
            } else if (routeList.get(i) instanceof WalkRoute) {
                sb.append("WALK");
            }
            if (i != routeList.size() - 1) {
                sb.append(" > ");
            }
        }
        return sb.toString();
    }

    public String getRouteString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < routeList.size(); i ++) {
            sb.append(routeList.get(i).getRouteString());
            if (i != routeList.size() - 1) {
                sb.append(".\n");
            }
        }
        return sb.toString();
    }


    public String getListViewRouteString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < routeList.size(); i ++) {
            sb.append(routeList.get(i).getRouteString());
            if (i != routeList.size() - 1) {
                sb.append(".\n\n");
            }
        }
        return sb.toString();
    }
    public String getAudioString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < routeList.size(); i ++) {
            sb.append(routeList.get(i).getAudioString());
            if (i != routeList.size() - 1) {
                sb.append(".\n");
            }
        }
        return sb.toString();
    }

    public static ArrayList<Itinerary> parseItinerariesJson(String json) {
        ArrayList<Itinerary> itineraryList = new ArrayList<>();
        try {
            JSONArray itineraries = new JSONArray(json);

            for (int i = 0; i < itineraries.length(); i ++) {
                JSONObject itinerary = itineraries.getJSONObject(i);
                JSONArray legs = itinerary.getJSONArray("legs");
                ArrayList<Route> routeList = new ArrayList<>();

                for (int j = 0; j < legs.length(); j ++) {
                    JSONObject leg = legs.getJSONObject(j);
                    String mode = leg.getString("mode");
                    String fromName = leg.getJSONObject("from").getString("name");
                    String toName = leg.getJSONObject("to").getString("name");
                    String polyline = leg.getJSONObject("legGeometry").getString("points");
                    Route routeObj;
                    switch (mode) {
                        case "BUS":
//                            routeObj = new BusRoute(fromName, toName, polyline, leg.getString("route"));
                            routeObj = createBusRoute(leg);
                            Log.d("RouteOptionsActivity", String.format("Take %s %s from %s to %s", ((BusRoute) routeObj).type, ((BusRoute) routeObj).busNumber, routeObj.from, routeObj.to));
                            break;
                        case "SUBWAY":
                            routeObj = createMrtRoute(leg);
                            Log.d("RouteOptionsActivity", String.format("Take %s (%s) from %s to %s", ((MrtRoute) routeObj).type, ((MrtRoute) routeObj).line, routeObj.from, routeObj.to));
                            break;
                        case "WALK":
                            routeObj = new WalkRoute(fromName, toName, polyline, leg.getJSONArray("steps").getJSONObject(0).getInt("distance"));
                            Log.d("RouteOptionsActivity", String.format("%s from %s to %s (%sm)", ((WalkRoute) routeObj).type, routeObj.from, routeObj.to, ((WalkRoute) routeObj).distance));
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + mode);
                    }
                    routeList.add(routeObj);
                }
                Itinerary itineraryObj = new Itinerary(routeList);
                itineraryList.add(itineraryObj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return itineraryList;
    }

    public static BusRoute createBusRoute(JSONObject leg) {
        try {
            ArrayList<Landmark> stopsList = new ArrayList<Landmark>();
            String fromName = leg.getJSONObject("from").getString("name");
            String fromCoord = leg.getJSONObject("from").getString("lat") + "," + leg.getJSONObject("from").getString("lon");

            String toName = leg.getJSONObject("to").getString("name");
            String toCoord = leg.getJSONObject("to").getString("lat") + "," + leg.getJSONObject("to").getString("lon");

            String busNumber = leg.getString("route");
            String polyline = leg.getJSONObject("legGeometry").getString("points");

            stopsList.add(new Landmark(fromCoord, LandmarkHandler.BUS_STOP_LETTER, LandmarkHandler.BUS_STOP_COLOR, LandmarkHandler.BUS_STOP_TYPE, fromName));

            JSONArray intermediateStops = leg.getJSONArray("intermediateStops");
            for (int i = 0; i < intermediateStops.length(); i ++) {
                JSONObject stop = intermediateStops.getJSONObject(i);
                String name = stop.getString("name");
                String coord = stop.getString("lat") + "," + stop.getString("lon");
                stopsList.add(new Landmark(coord, LandmarkHandler.BUS_STOP_LETTER, LandmarkHandler.BUS_STOP_COLOR, LandmarkHandler.BUS_STOP_TYPE, name));
            }

            stopsList.add(new Landmark(toCoord, LandmarkHandler.BUS_STOP_LETTER, LandmarkHandler.BUS_STOP_COLOR, LandmarkHandler.BUS_STOP_TYPE, toName));
            return new BusRoute(fromName, toName, polyline, busNumber, stopsList);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MrtRoute createMrtRoute(JSONObject leg) {
        try {
            ArrayList<Landmark> stopsList = new ArrayList<Landmark>();
            String fromName = leg.getJSONObject("from").getString("name");
            String fromCoord = leg.getJSONObject("from").getString("lat") + "," + leg.getJSONObject("from").getString("lon");

            String toName = leg.getJSONObject("to").getString("name");
            String toCoord = leg.getJSONObject("to").getString("lat") + "," + leg.getJSONObject("to").getString("lon");

            String line = leg.getString("route");
            String polyline = leg.getJSONObject("legGeometry").getString("points");

            stopsList.add(new Landmark(fromCoord, LandmarkHandler.MRT_STATION_LETTER, LandmarkHandler.MRT_STATION_COLOR, LandmarkHandler.MRT_STATION_TYPE, fromName));

            JSONArray intermediateStops = leg.getJSONArray("intermediateStops");
            for (int i = 0; i < intermediateStops.length(); i ++) {
                JSONObject stop = intermediateStops.getJSONObject(i);
                String name = stop.getString("name");
                String coord = stop.getString("lat") + "," + stop.getString("lon");
                stopsList.add(new Landmark(coord, LandmarkHandler.MRT_STATION_LETTER, LandmarkHandler.MRT_STATION_COLOR, LandmarkHandler.MRT_STATION_TYPE, name));
            }

            stopsList.add(new Landmark(toCoord, LandmarkHandler.MRT_STATION_LETTER, LandmarkHandler.MRT_STATION_COLOR, LandmarkHandler.MRT_STATION_TYPE, toName));
            return new MrtRoute(fromName, toName, polyline, line, stopsList);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
