package com.example.mymap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.mymap.Threads.DirectionQueryThread;
import com.example.mymap.Threads.LocationThread;
import com.example.mymap.Threads.SearchQueryThread;
import com.example.mymap.Utility.Settings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HomeUIActivity extends AppCompatActivity {
    Context context;
    EditText from_text;
    EditText to_text;
    boolean isEditing_from_text;
    boolean isEditing_to_text;
    String fromCoord;
    String toCoord;
    Button current_button;
    Button directions_button;
    ImageView loading_imageView;
    ListView search_listview;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    ArrayList<String> coordList;
    String token;
    boolean current_button_isClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_ui);

        checkPermissions();

        from_text = (EditText) findViewById(R.id.from_text);
        to_text = (EditText) findViewById(R.id.to_text);
        current_button = (Button) findViewById(R.id.current_button);
        directions_button = (Button) findViewById(R.id.directions_button);
        loading_imageView = (ImageView) findViewById(R.id.loading_imageView);


        search_listview = (ListView) findViewById(R.id.search_listview);
        listItems = new ArrayList<String>();
        adapter=new ArrayAdapter<String>(this,
                R.layout.search_listview,
                listItems);
        coordList = new ArrayList<String>();
        search_listview.setAdapter(adapter);
        search_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (isEditing_from_text) {
                    fromCoord = coordList.get(i);
                    from_text.setText(listItems.get(i));
                } else if (isEditing_to_text) {
                    toCoord = coordList.get(i);
                    to_text.setText(listItems.get(i));
                }

                if (fromCoord != null && toCoord != null) {
                    directions_button.setEnabled(true);
                    directions_button.setAlpha(1);
                }
            }
        });

        context = this;
        LocationThread locationThread = new LocationThread(this);
        locationThread.startLocationLooper();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        to_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isEditing_from_text = false;
                isEditing_to_text = true;
                Map<String, String> params = new HashMap<>();
                params.put("searchVal", charSequence.toString());
                params.put("returnGeom", "Y");
                params.put("getAddrDetails", "Y");

                Log.d("MainActivity", "current_button_isClicked: " + current_button_isClicked);
                SearchQueryThread searchQueryThread = new SearchQueryThread(context, params, adapter, coordList);
                searchQueryThread.start();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ;
            }
        });

        from_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() == 0 || current_button_isClicked) {
                    current_button_isClicked = false;
                    return;
                }
                isEditing_from_text = true;
                isEditing_to_text = false;
                Map<String, String> params = new HashMap<>();
                params.put("searchVal", charSequence.toString());
                params.put("returnGeom", "Y");
                params.put("getAddrDetails", "Y");

                Log.d("MainActivity", "directions_button.onClick()");
                SearchQueryThread searchQueryThread = new SearchQueryThread(context, params, adapter, coordList);
                searchQueryThread.start();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ;
            }
        });

        current_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_button_isClicked = true;
                Log.d("MainActivity", "current_button.onClick()");
                if (from_text.isEnabled()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Location currentLocation;
                            while (true) {
                                currentLocation = locationThread.currentLocation;
                                if (currentLocation == null) {

                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            loading_imageView.setVisibility(View.VISIBLE);
                                            current_button.setVisibility(View.GONE);
                                        }
                                    });

                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    fromCoord = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
                                    Log.d("MainActivity", "current_button.onClick() called" + currentLocation);
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            from_text.setText("(" + fromCoord + ")");
                                            from_text.setEnabled(false);
                                            current_button.setText("Cancel");
                                            loading_imageView.setVisibility(View.GONE);
                                            current_button.setVisibility(View.VISIBLE);

                                            if (fromCoord != null && toCoord != null) {
                                                directions_button.setEnabled(true);
                                                directions_button.setAlpha(1);
                                            }
                                        }
                                    });
                                    break;
                                }
                            }
                        }
                    }).start();

                } else {
                    from_text.setText("");
                    from_text.setEnabled(true);
                    current_button.setText("Current");
                }
            }
        });

        directions_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_button_isClicked = true;
                if (fromCoord == null && toCoord == null) {
                    return;
                }

                Map<String, String> params = new HashMap<>();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                dateFormat.format(date);

                params.put("routeType", "pt");
                params.put("token", Settings.onemapToken);
                params.put("time", timeFormat.format(date));
                params.put("date", dateFormat.format(date));
                params.put("numItineraries", "3");
                params.put("mode", "TRANSIT");
                params.put("start", fromCoord);
                params.put("end", toCoord);
                params.put("maxWalkDistance", "500");

//                GeoCalculator.measureString(fromCoord, toCoord);

                DirectionQueryThread directionQueryThread = new DirectionQueryThread(context, params, directions_button);
                directionQueryThread.start();
            }
        });

//        locationThread.start();
    }


    public void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 0);
        }
    }
}
