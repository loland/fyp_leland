package com.example.mymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.mymap.ListViewAdapters.RoutingBaseAdapter;
import com.example.mymap.Routes.Itinerary;

import java.util.ArrayList;

public class RouteOptionsActivity extends AppCompatActivity {

    ListView options_listview;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    ArrayList<Itinerary> itineraryList;
    RoutingBaseAdapter routingBaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_options);
        Log.d("RouteOptionsActivity", "spawned");

        itineraryList = Itinerary.parseItinerariesJson(getIntent().getStringExtra("itineraries"));

        Log.d("RouteOptionsActivity/onCreate", "number of itineraries: " + itineraryList.size());

        Log.d("RouteOptionsActivity/onCreate", ": " + itineraryList.get(0).getRouteTypesString());
        Log.d("RouteOptionsActivity/onCreate", ": " + itineraryList.get(0).getRouteString());

        options_listview = (ListView) findViewById(R.id.options_listview);
        listItems = new ArrayList<>();
        routingBaseAdapter = new RoutingBaseAdapter(getApplicationContext(), itineraryList);
        options_listview.setAdapter(routingBaseAdapter);

//
//        for (int i = 0; i < itineraryList.size(); i ++) {
//            adapter.add("Itinerary " + i);
//        }

        options_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("RouteOptionsActivity/onCreate/options_listview/onItemClick", "clicked");
                Itinerary itinerary = itineraryList.get(i);
                Intent intent = new Intent(getApplicationContext(), RoutingUIActivity.class);
                intent.putExtra("itinerary", itinerary);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        });

    }




}