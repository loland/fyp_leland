package com.example.mymap.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

public class Retraining {
    static public void storeToken(String token, Context context) {
        Log.d("Retraining/storeToken", "storing token: " + token);
        SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("jwtToken", token);
        editor.apply();

        getToken(context);
    }

    static public String getToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        String token = sharedPref.getString("jwtToken", null);
        Log.d("Retraining/storeToken", "retrieving token: " + token);
        return token;
    }

    static public void removeToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        preferences.edit().remove("jwtToken").apply();
    }

    static public boolean submitResult(String guid, boolean correct, String token) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("guid", guid);
            jsonObject.put("correct", correct);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject response = HttpRequest.makePostRequest(jsonObject.toString(), Settings.retrainingServer + Settings.validateImage, 3, token);
        try {
            if (response != null && response.getBoolean("success")) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

}