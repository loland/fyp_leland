package com.example.mymap.Threads;

import android.content.Context;

import com.example.mymap.Utility.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryThread extends Thread {
    Context context;
    List<String> urlList = null;
    List<JSONObject> jsonList = new ArrayList<>();

    public QueryThread(Context context, List<String> urlList) {
        this.context = context;
        this.urlList = urlList;
    }

    public QueryThread(Context context, String url) {
        this.context = context;
        this.urlList = Arrays.asList(url);
    }

    @Override
    public void run() {
        try {
            for (String url : urlList) {
                jsonList.add(HttpRequest.getJSONObjectFromURL(url));
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
