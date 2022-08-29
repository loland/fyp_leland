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
import com.example.mymap.Routes.Itinerary;
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
import java.util.Map;

public class RoutingUIThread extends RoutingThread {
    Map<String, String> params;
    ImageView mapView;
    String mapUrl = "https://developers.onemap.sg/commonapi/staticmap/getStaticImage";
    String lineAttributes = ":0,255,0:8";

    public RoutingUIThread(Context context, Itinerary itinerary, ImageView mapView, TextView distance_textView, Map<String, String> params, RoutingSettings routingSettings) {
        super(context, itinerary, distance_textView, routingSettings);
        this.currentRouteIndex = 0;
        this.mapView = mapView;
        this.runThread = true;
        this.polyList = PolylineDecoder.decodePolyToList(itinerary.getRoute(currentRouteIndex).polyline, null);
        this.removedPoints = new ArrayList<>();
        this.params = params;

        params.put("layerchosen", "default");
        if (!params.containsKey("zoom")) {
            params.put("zoom", "18");
        }
        params.put("height", "512");
        params.put("width", "512");
        params.put("points", "");
    }

    @Override
    public void updateCurrentLocation(boolean addInitial) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Log.d("RoutingUIThread/updateCurrentLocation", "fusedLocationClient: " + fusedLocationClient);
        Log.d("RoutingUIThread/updateCurrentLocation", "context: " + context);

        fusedLocationClient.getLastLocation().addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d("UpdateMapThread/updateCurrentLocation/onSuccess", "" + location);
                    params.put("lat", Double.toString(location.getLatitude()));
                    params.put("lng", Double.toString(location.getLongitude()));
                    currentCoords = params.get("lat") + "," + params.get("lng");
                    routingSettings.setCoords(currentCoords);

                    if (addInitial) {
                        polyList.add(0, currentCoords);

                        if (itinerary.getRoute(currentRouteIndex) instanceof WalkRoute) {
                            updateLandmarks(polyList.toArray(new String[0]));
                            distanceToNextPoint = GeoCalculator.compareString(currentCoords, polyList.get(1));
                            setDistanceToNextPointList(distanceToNextPoint);
                        }
                    }
                    updateLines();
                    Log.d("UpdateMapThread/updateCurrentLocation/onSuccess", "params: " + params);
                }
            }
        });
    }

    @Override
    public void updateLandmarks(String[] coords) {
        if (!(itinerary.getRoute(currentRouteIndex) instanceof WalkRoute)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                LandmarkHandler.getLandmarksStringFromPolyList(coords, params, landmarksList, context);
            }
        }).start();
    }

    public void updateLines() {
        params.put("lines", PolylineDecoder.decodePolyListToString(polyList) + lineAttributes);
    }


    public void mapRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("UpdateMapThread/mapRequest/run", "mapRequest run");
                String url;
                try {
                    url = mapUrl + ParameterStringBuilder.getParamsString(params);
                    Log.d("UpdateMapThread/mapRequest/run", "url: " + url);
                    Bitmap bitmap = HttpRequest.getBitmapFromUrl(url);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapView.setImageBitmap(bitmap);
                            Log.d("UpdateMapThread/mapRequest/runOnUiThread/run", "map set");
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public void run() {
        Log.d("UpdateMapThread/run", "thread running");
        while (runThread) {
            mapRequest();
            for (int i = 0; i < routingSettings.UPDATE_MAP_FREQUENCY; i++) {
                try {
                    sleep(1000);

                    if (routingSettings.manual) {
                        currentCoords = routingSettings.getCoords();
                        params.put("lat", routingSettings.getLat());
                        params.put("lng", routingSettings.getLon());
                    } else {
                        updateCurrentLocation(false);
                    }

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

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("UpdateMapThread/runOnUiThread/run", "location set");
//                            distance_textView.setText(String.format("%s\ndistance to next point: %dm", currentCoords, distance));
                            distance_textView.setText(String.format("%dm", distance));
                        }
                    });

                    updateLines();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
