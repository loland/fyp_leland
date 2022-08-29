package com.example.mymap.Threads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;

import com.example.mymap.Utility.ParameterStringBuilder;
import com.example.mymap.RouteOptionsActivity;
import com.example.mymap.Utility.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public class DirectionQueryThread extends Thread {
    public Context context;
    public Map<String, String> params;
    public String baseUrl = "https://developers.onemap.sg/privateapi/routingsvc/route";
    public Button directions_button;

    public DirectionQueryThread(Context context, Map<String, String> params, Button directions_button) {
        this.context = context;
        this.params = params;
        this.directions_button = directions_button;
    }

    public void run() {
        try {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    directions_button.setEnabled(false);
                    directions_button.setAlpha(0.5f);
                }
            });
            Log.d("DirectionQueryThread.queryDirections", "query url: " + baseUrl + ParameterStringBuilder.getParamsString(params));
            JSONObject jsonObj = HttpRequest.getJSONObjectFromURL(baseUrl + ParameterStringBuilder.getParamsString(params));
            JSONObject plan = jsonObj.getJSONObject("plan");
            JSONArray itineraries = plan.getJSONArray("itineraries");
            Intent intent = new Intent(context, RouteOptionsActivity.class);
            intent.putExtra("itineraries", itineraries.toString());
            context.startActivity(intent);
        } catch (IOException | JSONException e) {
            Log.d("DirectionQueryThread.queryDirections", "Error parsing JSON");
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    directions_button.setEnabled(true);
                    directions_button.setAlpha(1f);
                }
            });
            e.printStackTrace();
        }
    }

}
