package com.example.mymap.Threads;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.example.mymap.General.Landmark;
import com.example.mymap.General.RoutingSettings;
import com.example.mymap.HomeUIActivity;
import com.example.mymap.LandmarkDetectorActivity;
import com.example.mymap.Routes.BusRoute;
import com.example.mymap.Routes.Itinerary;
import com.example.mymap.Routes.MrtRoute;
import com.example.mymap.Routes.Route;
import com.example.mymap.Routes.WalkRoute;
import com.example.mymap.Utility.GeoCalculator;
import com.example.mymap.Utility.HttpRequest;
import com.example.mymap.Utility.LandmarkHandler;
import com.example.mymap.Utility.ParameterStringBuilder;
import com.example.mymap.Utility.PolylineDecoder;
import com.example.mymap.Utility.VoiceSpeaker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoutingThread extends Thread {
    Context context;
    public Itinerary itinerary;
    public int currentRouteIndex;
    public boolean runThread;
    public ArrayList<String> polyList;
    ArrayList<String> removedPoints;
    TextView distance_textView;
    FusedLocationProviderClient fusedLocationClient;
    public int degreesToNextPoint;
    String currentCoords;
    VoiceSpeaker voice;
    ArrayList<Integer> distanceToNextPointList;
    ArrayList<Landmark> landmarksList;
    public int degreesToTurn;
    int distanceToNextPoint;
    ArrayList<Landmark> stopList;
    RoutingSettings routingSettings;
    int MINIMUM_LANDMARK_DISTANCE;

    public RoutingThread(Context context, Itinerary itinerary, TextView distance_textView, RoutingSettings routingSettings) {
        this.context = context;
        this.itinerary = itinerary;
        this.currentRouteIndex = 0;
        this.distance_textView = distance_textView;
        this.runThread = true;
        this.polyList = PolylineDecoder.decodePolyToList(itinerary.getRoute(currentRouteIndex).polyline, null);
        this.removedPoints = new ArrayList<>();
        this.routingSettings = routingSettings;
        this.currentCoords = "";
        this.MINIMUM_LANDMARK_DISTANCE = 25;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        Log.d("RoutingThread", "fusedLocationClient initialized");
        distanceToNextPointList = new ArrayList<>();
        landmarksList = new ArrayList<Landmark>();
        voice = new VoiceSpeaker(context);
        updateCurrentLocation(true);

    }

    public void updateCurrentLocation(boolean addInitial) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d("UpdateMapThread/updateCurrentLocation/onSuccess", "" + location);
                    String lat = Double.toString(location.getLatitude());
                    String lon = Double.toString(location.getLongitude());
                    currentCoords = lat + "," + lon;

                    if (addInitial) {
                        polyList.add(0, currentCoords);

                        if (itinerary.getRoute(currentRouteIndex) instanceof WalkRoute) {
                            updateLandmarks(polyList.toArray(new String[0]));
                            distanceToNextPoint = GeoCalculator.compareString(currentCoords, polyList.get(1));
                            setDistanceToNextPointList(distanceToNextPoint);
                        }
                    }
                }
            }
        });
    }

    public int roundUp(int number) {
        if (number % 10 == 0) {
            return number;
        }
        return (((int) (number / 10)) * 10) + 10;
    }

    public void incrementCurrentRouteIndex() {
        currentRouteIndex += 1;
        if (currentRouteIndex == itinerary.getRouteCount()) {
            endItinerary();
            return;
        }

//        if (itinerary.getRoute(currentRouteIndex) instanceof BusRoute || itinerary.getRoute(currentRouteIndex) instanceof MrtRoute) {
//            isCurrentlyOnPublicTransport = true;
//        } else {
//            isCurrentlyOnPublicTransport = false;
//        }

        Log.d("UpdateMapThread/compareCurrentLocationToNextPoint", "currentRouteIndex incremented: " + currentRouteIndex);

        this.polyList = PolylineDecoder.decodePolyToList(itinerary.getRoute(currentRouteIndex).polyline, currentCoords);
        voice.sayWithoutFlush(itinerary.getRoute(currentRouteIndex).getRouteString());
        updateLandmarks(polyList.toArray(new String[0]));
    }

    public void voiceDirection() {
        voice.say(roundUp(distanceToNextPoint) + " metres to next point at " + GeoCalculator.degreesToClock(degreesToTurn) + " o clock");
    }

    public String getDirection() {
        return roundUp(distanceToNextPoint) + " metres to next point at " + GeoCalculator.degreesToClock(degreesToTurn) + " o clock";
    }

    public int compareCurrentLocationToNextPoint(boolean flush) {
        if (polyList.size() <= 1) {
            incrementCurrentRouteIndex();
        }

        int distance = GeoCalculator.compareString(currentCoords, polyList.get(1));
        if (distanceToNextPointList.size() > 0 && distance <= distanceToNextPointList.get(distanceToNextPointList.size() - 1)) {
            if (flush) {
                voice.say(roundUp(distance) + "metres to next point at " + GeoCalculator.degreesToClock(degreesToTurn) + " o clock");
            } else {
                voice.sayWithoutFlush(roundUp(distance) + "metres to next point at " + GeoCalculator.degreesToClock(degreesToTurn) + " o clock");
            }

            distanceToNextPointList.remove(distanceToNextPointList.size() - 1);
        }
        Log.d("UpdateMapThread/compareCurrentLocationToNextPoint", "distance to next point: " + distance + "m");

        if (distance <= 10) {
            removedPoints.add(polyList.get(0));
            polyList.remove(0);

            if (polyList.size() <= 1) {
                incrementCurrentRouteIndex();
                return 0;
            }

            distance = GeoCalculator.compareString(currentCoords, polyList.get(1));
            if (flush) {
                voice.say("point changed, " + roundUp(distance) + " metres to next point at " + GeoCalculator.degreesToClock(degreesToTurn) + " o clock");
            } else {
                voice.sayWithoutFlush("point changed, " + roundUp(distance) + " metres to next point at " + GeoCalculator.degreesToClock(degreesToTurn) + " o clock");
            }

            setDistanceToNextPointList(distance);
        }

        return distance;
    }

    public void setDistanceToNextPointList(int distance) {
        distanceToNextPointList.clear();
        for (int i = 10; i < distance; i += 10) {
            distanceToNextPointList.add(i);
        }
    }

    public void skipPoint() {
        if (polyList.size() == 0) {
            return;
        }
        this.removedPoints.add(polyList.get(0));
        this.polyList.remove(0);
    }

    public void unskipPoint() {
        if (removedPoints.size() == 0) {
            return;
        }
        int lastIndex = removedPoints.size() - 1;
        this.polyList.add(0, this.removedPoints.get(lastIndex));
        this.removedPoints.remove(lastIndex);
    }

    public void updateLandmarks(String[] coords) {
        if (!(itinerary.getRoute(currentRouteIndex) instanceof WalkRoute)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                LandmarkHandler.getLandmarksFromPolyList(coords, landmarksList, context);
            }
        }).start();
    }

    public void endItinerary() {
        Log.d("UpdateMapThread/endItinerary", "itinerary ended");
        voice.say("You have reached your destination");
        this.runThread = false;

        Intent intent = new Intent(context, HomeUIActivity.class);// New activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        ((Activity)context).finish();
    }

    public void forceEndItinerary() {
        Log.d("UpdateMapThread/endItinerary", "itinerary force ended");
        voice.say("Trip ended");
        this.runThread = false;

        Intent intent = new Intent(context, HomeUIActivity.class);// New activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        ((Activity)context).finish();
    }

    public boolean checkIfNearLandmark() {
        if (landmarksList.size() == 0) {
            return false;
        }

        Landmark nextLandmark = landmarksList.get(0);

        int minimumDetectionDistance;
        if (nextLandmark.detectionDistance != null) {
            minimumDetectionDistance = nextLandmark.detectionDistance;
            Log.d("RoutingThread/checkIfNearLandmark", "minimumDetectionDistance: " + nextLandmark.detectionDistance);
        } else {
            minimumDetectionDistance = MINIMUM_LANDMARK_DISTANCE;
        }

        int distance = GeoCalculator.compareString(currentCoords, nextLandmark.coord);

        if (distance > minimumDetectionDistance) {
            return false;
        }

        if (distance < 10) {
            landmarksList.remove(0);
            return false;
        }

        voice.sayWithoutFlush("approaching " + nextLandmark.type);

        if (routingSettings.autoCamera && !LandmarkDetectorActivity.active) {
            Intent intent = new Intent(context, LandmarkDetectorActivity.class);
            if (LandmarkHandler.modelMap.containsKey(nextLandmark.type)) {
                intent.putExtra("modelString", LandmarkHandler.modelMap.get(nextLandmark.type));
            }
            context.startActivity(intent);
            landmarksList.remove(0);
        }

        return true;
    }

    public Integer compareCurrentLocationToNextStop() {
        Route route = itinerary.getRoute(currentRouteIndex);
        if (route == null) {
            return null;
        }
        stopList = itinerary.getRoute(currentRouteIndex).getStopList();

        if (stopList.size() == 0) {
            return null;
        }

        Landmark nextStop = stopList.get(0);
        int distance = GeoCalculator.compareString(currentCoords, nextStop.coord);

        if (distance <= 20) {
            stopList.remove(0);

            if (stopList.size() == 0) {
                incrementCurrentRouteIndex();
                voice.sayWithoutFlush(String.format("Arrived at %s, alight now", nextStop.name));
                return null;
            }

            if (stopList.size() == 1) {
                voice.sayWithoutFlush(String.format("Arrived at %s, %s stop left. Alight at next stop.", nextStop.name, stopList.size()));
                return null;
            }

            voice.sayWithoutFlush(String.format("Arrived at %s, %s stops left.", nextStop.name, stopList.size()));
        }
        return distance;
    }


    @Override
    public void run() {
        Log.d("RoutingThread/run", "thread running");
        while (runThread) {
            try {
                sleep(1000);

                updateCurrentLocation(false);

                Integer distance;
                if (itinerary.getRoute(currentRouteIndex) instanceof WalkRoute) {
                    boolean isNearLandmark = checkIfNearLandmark();
                    distance = compareCurrentLocationToNextPoint(!isNearLandmark);

                    if (polyList.size() >= 2) {
                        degreesToNextPoint = GeoCalculator.degreeBearing(currentCoords, polyList.get(1));
                    }

                } else {
                    distance = compareCurrentLocationToNextStop();
                    if (distance != null) {
                        degreesToNextPoint = GeoCalculator.degreeBearing(currentCoords, stopList.get(0).coord);
                    }
                }

                if (distance == null) {
                    endItinerary();
                }

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("UpdateMapThread/runOnUiThread/run", "location set");
                        distance_textView.setText(String.format("%dm", distance));
                    }
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
