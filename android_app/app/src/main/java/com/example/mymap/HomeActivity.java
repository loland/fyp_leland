package com.example.mymap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.mymap.Routes.Itinerary;
import com.example.mymap.Threads.LocationThread;
import com.example.mymap.Utility.HttpRequest;
import com.example.mymap.Utility.Settings;
import com.example.mymap.Utility.ParameterStringBuilder;
import com.example.mymap.Utility.VoiceSpeaker;
import com.example.mymap.Utility.VoiceToText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    VoiceSpeaker voice;
    ImageView mic_imageView;
    VoiceToText voiceToText;
    Context context;
    String token = Settings.onemapToken;
    boolean expectingDestination_1 = true;
    boolean expectingDestinationConfirmation_2 = false;
    boolean expectingRouteNumber_3 = false;
    LocationThread locationThread;
    String toCoord;
    ArrayList<Itinerary> itineraryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;
        locationThread = new LocationThread(this);
        locationThread.startLocationLooper();

        voice = new VoiceSpeaker(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                voice.say("hello. Tap the center of the screen to speak. Where will be your destination?");
//                voice.say("hello");
                mic_imageView.setClickable(true);
            }
        });

        mic_imageView = findViewById(R.id.mic_imageView);
        mic_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceToText = new VoiceToText(context);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String text = Objects.requireNonNull(result).get(0);
                Log.d("HomeActivity/onActivityResult", text);

                if (expectingDestination_1) {
                    handleDestination(text);
                    expectingDestinationConfirmation();

                } else if (expectingDestinationConfirmation_2) {
                    if (text.equals("yes")) {
                        routeQuery();
                        expectingRouteNumber();
                    } else if (text.equals("no")) {
                        expectingDestination();
                        voice.say("Where will be your destination?");
                    }
                } else if (expectingRouteNumber_3) {
                    if (isNumber(text)) {
                        int index = Integer.parseInt(text);
                        index -= 1;
                        if (index >= 0 && index < itineraryList.size()) {
                            voice.say("You have chosen itinerary " + text);
                            startItinerary(itineraryList.get(index));
                        }
                    } else {
                        voice.say("not a number, please repeat");
                    }
                }
            }
        }
    }

    public void routeQuery() {
        locationThread.startLocationLooper();
        Location currentLocation = locationThread.currentLocation;
        String url = "https://developers.onemap.sg/privateapi/routingsvc/route";

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        dateFormat.format(date);

        String fromCoord = currentLocation.getLatitude() + "," + currentLocation.getLongitude();

        Map<String, String> params = new HashMap<>();
        params.put("routeType", "pt");
        params.put("token", token);
        params.put("time", timeFormat.format(date));
        params.put("date", dateFormat.format(date));
        params.put("numItineraries", "3");
        params.put("mode", "TRANSIT");
        params.put("start", fromCoord);
        params.put("end", toCoord);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObj = HttpRequest.getJSONObjectFromURL(url + ParameterStringBuilder.getParamsString(params));
                    JSONObject plan = jsonObj.getJSONObject("plan");
                    JSONArray itineraries = plan.getJSONArray("itineraries");
                    itineraryList = Itinerary.parseItinerariesJson(itineraries.toString());

                    StringBuilder sb = new StringBuilder();
//                    sb.append("There are ").append(itineraryList.size()).append(" possible routes.");
                    sb.append(String.format("There are %s possible routes.", itineraryList.size()));
                    for (int i = 0; i < itineraryList.size(); i ++) {
                        Itinerary itinerary = itineraryList.get(i);
//                        voice.say("route number " + i + ". " + itinerary.getRouteString());
                        sb.append(String.format("Route number %s. %s. ", i + 1, itinerary.getAudioString()));
                    }

                    voice.say(sb.toString());
                    voice.sayWithoutFlush("Please say your preferred route number to start");

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void handleDestination(String text) {
        String url = "https://developers.onemap.sg/commonapi/search";

        Map<String, String> params = new HashMap<>();
        params.put("searchVal", text);
        params.put("returnGeom", "Y");
        params.put("getAddrDetails", "Y");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject[] jsonObj = {HttpRequest.getJSONObjectFromURL(url + ParameterStringBuilder.getParamsString(params))};
                    JSONArray jsonArr = jsonObj[0].getJSONArray("results");
                    JSONObject match = jsonArr.getJSONObject(0);
                    String value = match.getString("SEARCHVAL");
                    String lat = match.getString("LATITUDE");
                    String lon = match.getString("LONGITUDE");
                    toCoord = lat + "," + lon;

                    StringBuilder postalString = new StringBuilder();
                    String postalCode = match.getString("POSTAL");
                    for (char chr : postalCode.toCharArray()) {
                        postalString.append(chr).append(" ");
                    }
                    voice.say("found " + value + " at postal code " + postalString + ". Say yes to confirm destination, or no to cancel.");

                } catch (JSONException | IOException e) {
                    expectingDestination_1 = true;
                    expectingDestinationConfirmation_2 = false;
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void startItinerary(Itinerary itinerary) {
        Intent intent = new Intent(getApplicationContext(), RoutingActivity.class);
        intent.putExtra("itinerary", itinerary);
        this.startActivity(intent);
    }

    public void expectingDestination() {
        expectingDestination_1 = true;
        expectingDestinationConfirmation_2 = false;
        expectingRouteNumber_3 = false;
    }

    public void expectingDestinationConfirmation() {
        expectingDestination_1 = false;
        expectingDestinationConfirmation_2 = true;
        expectingRouteNumber_3 = false;
    }

    public void expectingRouteNumber() {
        expectingDestination_1 = false;
        expectingDestinationConfirmation_2 = false;
        expectingRouteNumber_3 = true;
    }

    public static boolean isNumber(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}