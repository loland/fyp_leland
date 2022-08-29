package com.example.mymap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mymap.General.RoutingSettings;
import com.example.mymap.Routes.Itinerary;
import com.example.mymap.Threads.RoutingThread;
import com.example.mymap.Threads.RoutingUIThread;
import com.example.mymap.Utility.LandmarkHandler;
import com.example.mymap.Utility.VoiceSpeaker;
import com.example.mymap.Utility.VoiceToText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RoutingActivity extends AppCompatActivity implements SensorEventListener {
    Itinerary itinerary;
    public int currentRoute;
    RoutingThread routingThread;
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
    RoutingSettings routingSettings;
    LinearLayout routing_linearLayout;
    VoiceSpeaker voice;
    VoiceToText voiceToText;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing);

        itinerary = (Itinerary) getIntent().getSerializableExtra("itinerary");
        currentRoute = 0;

        distance_textView = findViewById(R.id.distance_textView);
        direction_pointer = findViewById(R.id.direction_pointer);

        routingSettings = new RoutingSettings();

        routingThread = new RoutingThread(this, itinerary, distance_textView, routingSettings);
        routingThread.start();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        context = this;

        voice = new VoiceSpeaker(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                voice.say("Routing started. Tap anywhere on the screen to speak. Say help for commands.");
            }
        });

        routing_linearLayout = (LinearLayout) findViewById(R.id.routing_linearLayout);
        routing_linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceToText = new VoiceToText(context);
            }
        });

        start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String text = Objects.requireNonNull(result).get(0);

                StringBuilder response = new StringBuilder();
                if (text.equals("help")) {
                    response.append(
                            "Say directions for navigation help. " +
                            "Say traffic to open traffic light detection camera. " +
                            "Say bus to open bus stop detection camera. " +
                            "Say current for current route. " +
                            "Say remaining for remaining route. "
                    );

                } else if (text.equals("directions")) {
                    response.append(routingThread.getDirection());

                } else if (text.equals("traffic")) {
                    response.append("Opening traffic light detection camera");
                    Intent intent = new Intent(context, LandmarkDetectorActivity.class);
                    intent.putExtra("modelString", LandmarkHandler.modelMap.get(LandmarkHandler.TRAFFIC_LIGHT_TYPE));
                    intent.putExtra("colors", LandmarkHandler.colorMap.get(LandmarkHandler.TRAFFIC_LIGHT_COLOR));
                    context.startActivity(intent);

                } else if (text.equals("bus")) {
                    response.append("Opening bus stop detection camera");
                    Intent intent = new Intent(context, LandmarkDetectorActivity.class);
                    intent.putExtra("modelString", LandmarkHandler.modelMap.get(LandmarkHandler.BUS_STOP_TYPE));
                    intent.putExtra("colors", LandmarkHandler.colorMap.get(LandmarkHandler.BUS_STOP_COLOR));
                    context.startActivity(intent);

                } else if (text.equals("nlb")) {
                    response.append("Opening nlb detection camera");
                    Intent intent = new Intent(context, LandmarkDetectorActivity.class);
                    intent.putExtra("modelString", LandmarkHandler.modelMap.get(LandmarkHandler.NLB_LOGO_TYPE));
                    intent.putExtra("colors", LandmarkHandler.colorMap.get(LandmarkHandler.NLB_LOGO_COLOR));
                    context.startActivity(intent);

                } else if (text.equals("current")) {
                    response.append(routingThread.itinerary.getRoute(routingThread.currentRouteIndex).getRouteString());

                } else if (text.equals("remaining")) {
                    Itinerary itinerary = routingThread.itinerary;
                    for (int i = routingThread.currentRouteIndex; i < itinerary.getRouteCount(); i ++) {
                        response.append(itinerary.getRoute(i).getRouteString());
                        response.append(". ");
                    }

                } else {
                    response.append("Invalid command. Say help for commands");
                }

                voice.sayWithoutFlush(response.toString());

            }
        }
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
        int degreesToTurn = ((routingThread.degreesToNextPoint - azimuth) + 360) % 360;
        routingThread.degreesToTurn = degreesToTurn;
        direction_pointer.setRotation(degreesToTurn);
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
        routingThread.runThread = false;
    }
}