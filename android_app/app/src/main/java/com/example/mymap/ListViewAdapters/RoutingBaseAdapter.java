package com.example.mymap.ListViewAdapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mymap.R;
import com.example.mymap.Routes.Itinerary;

import java.util.ArrayList;
import java.util.List;

public class RoutingBaseAdapter extends BaseAdapter {
    Context context;
    ArrayList<Itinerary> itineraryList;
    LayoutInflater inflater;
    int[] colors = {
            Color.rgb(200,60,76),
            Color.rgb(240,156,84),
            Color.rgb(88,164,76),
            Color.rgb(80,124,252),
            Color.rgb(152,108,212)
    };

    public RoutingBaseAdapter(Context context, ArrayList<Itinerary> itineraryList) {
        this.context = context;
        this.itineraryList = itineraryList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return itineraryList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.activity_routing_list_view, null);
        TextView routeDetails = view.findViewById(R.id.routeDetails);
        TextView routeInstructions = view.findViewById(R.id.routeInstructions);

        LinearLayout route_linearLayout = view.findViewById(R.id.route_linearLayout);
        route_linearLayout.setBackgroundColor(colors[i]);
        routeDetails.setText(itineraryList.get(i).getRouteTypesString());
//        routeDetails.setTextColor(colors[i]);
//
        routeInstructions.setText(itineraryList.get(i).getListViewRouteString());
//        routeInstructions.setTextColor(colors[i]);

        return view;
    }
}
