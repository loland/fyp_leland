package com.example.mymap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.mymap.General.RoutingSettings;
import com.example.mymap.Routes.Itinerary;
import com.example.mymap.Threads.RoutingUIThread;
import com.example.mymap.Utility.VoiceSpeaker;

import java.util.HashMap;
import java.util.Map;

public class RoutingUIActivity extends AppCompatActivity implements SensorEventListener {
    Itinerary itinerary;
    int currentRoute;
    RoutingUIThread routingUIThread;
    ImageView mapView;
    ImageView settings;
    int azimuth;
    public SensorManager sensorManager;
    public Sensor rotationVector;
    public Sensor accelerometer;
    public Sensor magnetometer;
    public float[] rMatrix = new float[9];
    public float[] orientation = new float[9];
    public float[] lastAccelerometer = new float[3];
    public float[] lastMagnetometer = new float[3];
    public boolean hasSensor = false;
    public boolean hasSensor2 = false;
    public boolean lastAccelerationSet = false;
    public boolean lastMagnetSet = false;
    public ImageView direction_pointer;
    public TextView distance_textView;
    Dialog dialog;
    Map<String, String> params;
    RoutingSettings routingSettings;
    TextView north;
    TextView south;
    TextView east;
    TextView west;
    Button camera_button;
    Context context;
    Button direction_button;
//    Button alighted_button;
    Button endTrip_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing_ui);

        mapView = findViewById(R.id.mapView);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int width = (int) Math.sqrt(2 * Math.pow(screenWidth, 2));
        int height = width;
        Log.d("RoutingUIActivity/onCreate", "screenWidth: " + screenWidth);
        Log.d("RoutingUIActivity/onCreate", "new width: " + width);
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, width);
//        mapView.setLayoutParams(layoutParams);
        mapView.getLayoutParams().width = width;
        mapView.getLayoutParams().height = height;
        mapView.requestLayout();

        context = this;

        itinerary = (Itinerary) getIntent().getSerializableExtra("itinerary");
        currentRoute = 0;

        distance_textView = findViewById(R.id.distance_textView);
        direction_pointer = findViewById(R.id.direction_pointer);
        settings = findViewById(R.id.settings);

        camera_button = (Button) findViewById(R.id.camera_button);
        direction_button = findViewById(R.id.direction_button);
        endTrip_button = findViewById(R.id.endTrip_button);
//        alighted_button = findViewById(R.id.alighted_button);

        direction_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                routingUIThread.voiceDirection();
            }
        });


        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetectorActivity.class);
                context.startActivity(intent);
            }
        });

//        alighted_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ;
//            }
//        });

        params = new HashMap<>();

        routingSettings = new RoutingSettings();
        routingSettings.manual = false;

        north = findViewById(R.id.north);
        south = findViewById(R.id.south);
        east = findViewById(R.id.east);
        west = findViewById(R.id.west);

        routingUIThread = new RoutingUIThread(this, itinerary, mapView, distance_textView, params, routingSettings);
        routingUIThread.start();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        start();

        dialog = new Dialog(this);


        endTrip_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setContentView(R.layout.end_itinerary);
                dialog.show();

                Button yes_button = dialog.findViewById(R.id.yes_button);
                Button no_button = dialog.findViewById(R.id.no_button);

                yes_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        routingUIThread.forceEndItinerary();
                    }
                });

                no_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });


        settings.setClickable(true);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setContentView(R.layout.activity_routing_settings);
                dialog.show();

                SeekBar zoomSeekBar = dialog.findViewById(R.id.zoomSeekBar);
                Switch audioSwitch = dialog.findViewById(R.id.audioSwitch);
                TextView closeSettings = dialog.findViewById(R.id.closeSettings);
                Switch manualSwitch = dialog.findViewById(R.id.manualSwitch);
                Switch cameraSwitch = dialog.findViewById(R.id.cameraSwitch);

                TextView pointsLeftTextView = dialog.findViewById(R.id.pointsLeftTextView);
                ImageView addPoint = dialog.findViewById(R.id.addPoint);
                ImageView removePoint = dialog.findViewById(R.id.removePoint);

                TextView mapUpdateTextView = dialog.findViewById(R.id.mapUpdateTextView);
                ImageView mapUpdateAddPoint = dialog.findViewById(R.id.mapUpdateAddPoint);
                ImageView mapUpdateRemovePoint = dialog.findViewById(R.id.mapUpdateRemovePoint);

                pointsLeftTextView.setText(String.valueOf(routingUIThread.polyList.size()));
                mapUpdateTextView.setText(String.valueOf(routingSettings.UPDATE_MAP_FREQUENCY));

                if (params.containsKey("zoom")) {
                    zoomSeekBar.setProgress(Integer.parseInt(params.get("zoom")) - 11);
                }

                audioSwitch.setChecked(VoiceSpeaker.audio);
                cameraSwitch.setChecked(routingSettings.autoCamera);
                manualSwitch.setChecked(routingSettings.manual);


                int visibility;
                if (routingSettings.manual) {
                    visibility = View.VISIBLE;
                } else {
                    visibility = View.INVISIBLE;
                }
                north.setVisibility(visibility);
                south.setVisibility(visibility);
                east.setVisibility(visibility);
                west.setVisibility(visibility);

                removePoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        routingUIThread.skipPoint();
                        pointsLeftTextView.setText(String.valueOf(routingUIThread.polyList.size()));
                    }
                });

                addPoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        routingUIThread.unskipPoint();
                        pointsLeftTextView.setText(String.valueOf(routingUIThread.polyList.size()));
                    }
                });

                north.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        routingSettings.incLat();
                    }
                });
                south.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        routingSettings.decLat();
                    }

                });
                east.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        routingSettings.incLon();
                    }
                });
                west.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        routingSettings.decLon();
                    }
                });


                Log.d("RoutingActivity/onCreate/onClick", "closeSettings: " + closeSettings);
                closeSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        i += 11;
                        params.put("zoom", String.valueOf(i));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                cameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        routingSettings.autoCamera = b;
                    }
                });

                audioSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        VoiceSpeaker.audio = b;
                    }
                });

                manualSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        routingSettings.manual = b;
                        int visibility;
                        if (b) {
                            visibility = View.VISIBLE;
                        } else {
                            visibility = View.INVISIBLE;
                        }
                        north.setVisibility(visibility);
                        south.setVisibility(visibility);
                        east.setVisibility(visibility);
                        west.setVisibility(visibility);
                    }
                });

                mapUpdateAddPoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        routingSettings.UPDATE_MAP_FREQUENCY += 1;
                        mapUpdateTextView.setText(String.valueOf(routingSettings.UPDATE_MAP_FREQUENCY));
                    }
                });

                mapUpdateRemovePoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (routingSettings.UPDATE_MAP_FREQUENCY <= 1) {
                            return;
                        }

                        routingSettings.UPDATE_MAP_FREQUENCY -= 1;
                        mapUpdateTextView.setText(String.valueOf(routingSettings.UPDATE_MAP_FREQUENCY));
                    }
                });


            }
        });


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMatrix, event.values);
            azimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMatrix, orientation)[0]) + 360) % 360);
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerationSet = true;
        } else {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
                lastMagnetSet = true;
            }

            if (lastMagnetSet && lastAccelerationSet) {
                SensorManager.getRotationMatrix(rMatrix, null, lastAccelerometer, lastMagnetometer);
                SensorManager.getOrientation(rMatrix, orientation);
                azimuth = (int) ((Math.toDegrees(SensorManager.getOrientation(rMatrix, orientation)[0]) + 360) % 360);
            }
        }

        azimuth = Math.round(azimuth);
        int degreesToTurn = ((routingUIThread.degreesToNextPoint - azimuth) + 360) % 360;
        routingUIThread.degreesToTurn = degreesToTurn;
        direction_pointer.setRotation(degreesToTurn);
        mapView.setRotation(-azimuth);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void start() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null || sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
                Log.d("RoutingActivity/start", "no sensors");
            } else {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

                hasSensor = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                hasSensor2 = sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        } else {
            rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            hasSensor = sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        if (hasSensor && hasSensor2) {
            sensorManager.unregisterListener(this, accelerometer);
            sensorManager.unregisterListener(this, magnetometer);
        } else if (hasSensor) {
            sensorManager.unregisterListener(this, rotationVector);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        routingUIThread.runThread = false;
    }
}

