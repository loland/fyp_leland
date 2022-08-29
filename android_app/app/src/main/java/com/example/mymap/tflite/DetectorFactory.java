package com.example.mymap.tflite;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.mymap.Utility.HttpRequest;
import com.example.mymap.Utility.ParameterStringBuilder;
import com.example.mymap.Utility.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class DetectorFactory {
    public static YoloV5Classifier getDetector(final AssetManager assetManager, final String modelFilename, Context context) throws IOException {
        String labelFilename = null;
        String detectorName = "";
        boolean isQuantized = false;
        int inputSize = 0;

        if (modelFilename.equals("nlb_logo.tflite")) {
            labelFilename = "file:///android_asset/nlb_logo.txt";
            isQuantized = false;
            inputSize = 640;
            detectorName = "nlb_logo";
        }

        else if (modelFilename.equals("red_green_man.tflite")) {
            labelFilename = "file:///android_asset/red_green_man.txt";
            isQuantized = false;
            inputSize = 640;
            detectorName = "red_green_man";
        }

        else if (modelFilename.equals("bus_stop.tflite")) {
            labelFilename = "file:///android_asset/bus_stop.txt";
            isQuantized = false;
            inputSize = 640;
            detectorName = "bus_stop";
        }

        if (detectorName.equals("")) {
            return null;
        }
        return YoloV5Classifier.create(assetManager, modelFilename, labelFilename, isQuantized, inputSize, detectorName, context);
    }


    public static void requestModelUpdates(Context context) {
        String[] detectors = {"nlb_logo", "red_green_man", "bus_stop"};
        Map<String, String> params = new HashMap<String, String>();

        JSONObject temp = new JSONObject();
        for (String detector : detectors) {
            SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
            String timestamp = sharedPref.getString(detector, "0");
            try {
                temp.put(detector, timestamp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        params.put("modelType", "tflite");
        params.put("detectors", temp.toString());

        try {
            Log.d("DetectorFactory/requestModelUpdates", "params: " + ParameterStringBuilder.getParamsString(params));
            JSONObject response =  HttpRequest.makeGetRequest(Settings.retrainingServer + Settings.getWeightsNeeded + ParameterStringBuilder.getParamsString(params));
            if (response == null) {
                return;
            }
            if (! (response.has("success") && response.getBoolean("success"))) {
                return;
            }

            JSONObject detectorResponse = response.getJSONObject("detectors");
            Log.d("DetectorFactory/requestModelUpdates", "weights needed: " + detectorResponse.names());



            JSONArray keys = detectorResponse.names();
            if (keys == null) {
                return;
            }

            for (int i = 0; i < keys.length(); i ++) {
                params = new HashMap<String, String>();
                params.put("detector", keys.getString(i));
                params.put("modelType", "tflite");

                String detector = keys.getString(i);

                response = HttpRequest.makeGetRequest(Settings.retrainingServer + Settings.getIndividualWeight + ParameterStringBuilder.getParamsString(params));
                if (response == null) {
                    return;
                }
                if (! (response.has("success") && response.getBoolean("success"))) {
                    return;
                }


                String weightB64 = response.getString("weight");
                String timestamp = response.getString("timestamp");

                SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
                sharedPref.edit().putString(detector, timestamp).apply();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    String filename = detector + ".tflite";
                    byte[] weightBytes = Base64.getDecoder().decode(weightB64);
                    FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                    fos.write(weightBytes);
                    Log.d("DetectorFactory/requestModelUpdates", "timestamp: " + timestamp + ", detector: " + detector + ", received");
                }
            }


//            if (keys == null) {
//                return;
//            }
//
//            for (int i = 0; i < keys.length(); i ++) {
//                String detector = keys.getString(i);
//
//                String timestamp = weights.getJSONObject(detector).getString("timestamp");
//                String weightB64 = weights.getJSONObject(detector).getString("weight");
//
//                Log.d("DetectorFactory/requestModelUpdates", "timestamp: " + timestamp + " length of weightB64: " + weightB64.length());
//
//                SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
//                sharedPref.edit().putString(detector, timestamp).apply();
//
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    String filename = detector + ".tflite";
//                    byte[] weightBytes = Base64.getDecoder().decode(weightB64);
//                    FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
//                    fos.write(weightBytes);
//                }
//            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }



//    public static void requestModelUpdates(Context context) {
//        String[] detectors = {"nlb_logo", "red_green_man", "bus_stop"};
//        Map<String, String> params = new HashMap<String, String>();
//
//        JSONObject temp = new JSONObject();
//        for (String detector : detectors) {
//            SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
//            String timestamp = sharedPref.getString(detector, "0");
//            try {
//                temp.put(detector, timestamp);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        params.put("modelType", "tflite");
//        params.put("detectors", temp.toString());
//
//        try {
//            Log.d("DetectorFactory/requestModelUpdates", "params: " + ParameterStringBuilder.getParamsString(params));
//            JSONObject response =  HttpRequest.makeGetRequest(Settings.retrainingServer + Settings.getWeights + ParameterStringBuilder.getParamsString(params));
//            if (response == null) {
//                return;
//            }
//            if (! (response.has("success") && response.getBoolean("success"))) {
//                return;
//            }
//
//            JSONObject weights = response.getJSONObject("weights");
//            Log.d("DetectorFactory/requestModelUpdates", "weights received: " + weights.names());
//
//            JSONArray keys = weights.names();
//            if (keys == null) {
//                return;
//            }
//
//            for (int i = 0; i < keys.length(); i ++) {
//                String detector = keys.getString(i);
//
//                String timestamp = weights.getJSONObject(detector).getString("timestamp");
//                String weightB64 = weights.getJSONObject(detector).getString("weight");
//
//                Log.d("DetectorFactory/requestModelUpdates", "timestamp: " + timestamp + " length of weightB64: " + weightB64.length());
//
//                SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
//                sharedPref.edit().putString(detector, timestamp).apply();
//
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    String filename = detector + ".tflite";
//                    byte[] weightBytes = Base64.getDecoder().decode(weightB64);
//                    FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
//                    fos.write(weightBytes);
//                }
//            }
//
//        } catch (JSONException | IOException e) {
//            e.printStackTrace();
//        }
//    }

}
