package com.example.mymap.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mymap.General.Landmark;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LandmarkHandler {
    public static String OVERPASS_URL = "https://overpass-api.de/api/interpreter";
    public static String BASE_QUERY = "[out:json];(node(around:20,%s,%s););out center;";
//    public static String BASE_QUERY = "[out:json];(node[\"highway\"~\"bus_stop|traffic_signals\"](around:20,%s,%s););out center;";

    public static String BUS_STOP_COLOR = "\"255,255,178\"";
    public static String BUS_STOP_LETTER = "\"B\"";
    public static String BUS_STOP_TYPE = "bus stop";

    public static String TRAFFIC_LIGHT_COLOR = "\"175,50,0\"";
    public static String TRAFFIC_LIGHT_LETTER = "\"T\"";
    public static String TRAFFIC_LIGHT_TYPE = "traffic light";

    public static String MRT_STATION_COLOR = "\"0,50,175\"";
    public static String MRT_STATION_LETTER = "\"M\"";
    public static String MRT_STATION_TYPE = "mrt station";

    public static String NLB_LOGO_COLOR = "\"80,124,252\"";
    public static String NLB_LOGO_LETTER = "\"L\"";
    public static String NLB_LOGO_TYPE = "nlb logo";

    public static Map<String, String> modelMap;
    public static Map<String, int[]> colorMap;

    static {
        modelMap = new HashMap<String, String>();
        modelMap.put(BUS_STOP_TYPE, "bus_stop.tflite");
        modelMap.put(TRAFFIC_LIGHT_TYPE, "red_green_man.tflite");
        modelMap.put(NLB_LOGO_TYPE, "nlb_logo.tflite");
    }


    public static void getLandmarksFromPolyList(String[] coordList, ArrayList<Landmark> landmarksList, Context context) {
        Map<String, Landmark> map = new HashMap<>();
        Landmark[] temp;
        for (String coord : coordList) {
            temp = getLandmarks(coord, context);
            for (Landmark landmark : temp) {
                if (!(map.containsKey(landmark.coord))) {
                    landmarksList.add(landmark);
                    map.put(landmark.coord, landmark);
                }
            }
        }
    }

    public static void getLandmarksStringFromPolyList(String[] coordList, Map<String, String> params, ArrayList<Landmark> landmarksList, Context context) {
        Map<String, Landmark> map = new HashMap<>();
        Landmark[] temp;
        for (String coord : coordList) {
            temp = getLandmarks(coord, context);
            for (Landmark landmark : temp) {
                if (!(map.containsKey(landmark.coord))) {
                    landmarksList.add(landmark);
                    map.put(landmark.coord, landmark);
                    String sb = "|[" +  landmark.coord + "," + landmark.color + "," + landmark.letter + "]";
                    String newPoints = params.get("points") + sb;
                    if (newPoints.charAt(0) == '|') {
                        newPoints = newPoints.substring(1);
                    }
                    params.put("points", newPoints);
                }
            }
        }
    }

    public static Landmark[] getLandmarks(String coords, Context context) {
        ArrayList<Landmark> landmarks = new ArrayList<>();
        JSONObject jsonObject;

        // checks cache for any landmarks at coordinate, if none found, returns null
        jsonObject = getLandmarksFromCache(coords, context);

        if (jsonObject == null) {
            String[] coordsList = coords.split(",", 2);
            double lat = Double.parseDouble(coordsList[0]);
            double lng = Double.parseDouble(coordsList[1]);
            Map<String, String> params = new HashMap<>();
            params.put("data", String.format(BASE_QUERY, lat, lng));

            try {
                String url = OVERPASS_URL + ParameterStringBuilder.getParamsString(params);
                jsonObject = HttpRequest.getJSONObjectFromURL(url);
                addLandmarksToCache(coords, context, jsonObject);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }


        if (jsonObject == null) {
            return landmarks.toArray(new Landmark[0]);
        }

        try {
            JSONArray elements = jsonObject.getJSONArray("elements");

            for (int i = 0; i < elements.length(); i ++) {
                JSONObject element = elements.getJSONObject(i);

                if (!element.has("tags")) {
                    continue;
                }

                JSONObject tags = element.getJSONObject("tags");

                String name = null;
                if (tags.has("highway")) {
                    name = tags.getString("highway");
                } else if (tags.has("name")) {
                    name = tags.getString("name");
                }

                if (name == null) {
                    continue;
                }

                String coord = element.getDouble("lat") + "," + element.getDouble("lon");

                Landmark landmark;
                switch (name) {
                    case "bus_stop":
                        landmark = new Landmark(coord, BUS_STOP_LETTER, BUS_STOP_COLOR, BUS_STOP_TYPE);
                        break;

                    case "traffic_signals":
                        landmark = new Landmark(coord, TRAFFIC_LIGHT_LETTER, TRAFFIC_LIGHT_COLOR, TRAFFIC_LIGHT_TYPE);
                        break;

                    case "nlb_logo":
                        landmark = new Landmark(coord, NLB_LOGO_LETTER, NLB_LOGO_COLOR, NLB_LOGO_TYPE);
                        break;

                    default:
                        continue;
                }

                landmarks.add(landmark);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return landmarks.toArray(new Landmark[0]);
    }

    public static JSONObject getLandmarksFromCache(String coords, Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(Settings.landmarksSharedPref, Context.MODE_PRIVATE);
        String json = sharedPrefs.getString(coords, null);
        Log.d("LandmarkHandler/getLandmarksFromCache", "cache for " + coords + " retrieved");

        if (json == null) {
            return null;
        }

        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addLandmarksToCache(String coords, Context context, JSONObject jsonObject) {
        SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        if (!sharedPref.getBoolean("enableLandmarkCache", true)) {
            return;
        }

        SharedPreferences sharedPrefs = context.getSharedPreferences(Settings.landmarksSharedPref, Context.MODE_PRIVATE);
        sharedPrefs.edit().putString(coords, jsonObject.toString()).apply();
        Log.d("LandmarkHandler/addLandmarksFromCache", "cache for " + coords + " added");
    }

}
