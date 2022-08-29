package com.example.mymap.Threads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.mymap.Utility.HttpRequest;
import com.example.mymap.Utility.ParameterStringBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchQueryThread extends Thread {
    public Context context;
    public Map<String, String> params;
    public String baseUrl = "https://developers.onemap.sg/commonapi/search";
    public ArrayAdapter<String> adapter;
    public ArrayList<String> coordList;

    public SearchQueryThread(Context context, Map<String, String> params, ArrayAdapter<String> adapter, ArrayList<String> coordList) {
        this.context = context;
        this.params = params;
        this.adapter = adapter;
        this.coordList = coordList;
    }

    @Override
    public void run() {
        try {
            final JSONObject[] jsonObj = {HttpRequest.getJSONObjectFromURL(baseUrl + ParameterStringBuilder.getParamsString(params))};
            JSONArray jsonArr = jsonObj[0].getJSONArray("results");

            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.clear();
                    coordList.clear();
                    String tempString;
                    Map<String, String> tempMap = new HashMap<>();
                    for (int i = 0; i < jsonArr.length(); i ++) {
                        try {
                            jsonObj[0] = jsonArr.getJSONObject(i);
                            tempString = jsonObj[0].getString("SEARCHVAL");
                            if (!tempMap.containsKey(tempString)) {
                                adapter.add(tempString);
                                tempMap.put(tempString, "");
                                coordList.add(jsonObj[0].getString("LATITUDE") + "," + jsonObj[0].getString("LONGITUDE"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            Log.d("SearchQueryThread", "run, jsonArr: " + jsonArr.toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}
