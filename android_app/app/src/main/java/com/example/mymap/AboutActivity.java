package com.example.mymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.mymap.Utility.Settings;

import org.w3c.dom.Text;

import java.io.File;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Context context = this;

        TextView nlb_logo_textView = findViewById(R.id.nlb_logo_textView);
        TextView red_green_man_textView = findViewById(R.id.red_green_man_textView);
        TextView bus_stop_textView = findViewById(R.id.bus_stop_textView);
        Switch sendImages_switch = findViewById(R.id.sendImages_switch);
        Switch downloadWeights_switch = findViewById(R.id.downloadWeights_switch);
        Switch landmarkCache_switch = findViewById(R.id.landmarkCache_switch);
        Button clearWeights_button = findViewById(R.id.clearWeights_button);

        SharedPreferences sharedPref = this.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        nlb_logo_textView.setText(String.format("%s (%s)", "nlb_logo.tflite", sharedPref.getString("nlb_logo", "Default")));
        red_green_man_textView.setText(String.format("%s (%s)", "red_green_man.tflite", sharedPref.getString("red_green_man", "Default")));
        bus_stop_textView.setText(String.format("%s (%s)", "bus_stop.tflite", sharedPref.getString("bus_stop", "Default")));

        clearWeights_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File file = new File(context.getFilesDir(), "nlb_logo.tflite");
                if (file.exists() && file.delete()) {
                    Log.d("AboutActivity/onCreate", "successfully deleted nlb_logo.tflite");
                    sharedPref.edit().remove("nlb_logo").apply();
                }

                file = new File(context.getFilesDir(), "red_green_man.tflite");
                if (file.exists() && file.delete()) {
                    Log.d("AboutActivity/onCreate", "successfully deleted red_green_man.tflite");
                    sharedPref.edit().remove("red_green_man").apply();
                }

                file = new File(context.getFilesDir(), "bus_stop.tflite");
                if (file.exists() && file.delete()) {
                    Log.d("AboutActivity/onCreate", "successfully deleted bus_stop.tflite");
                    sharedPref.edit().remove("bus_stop").apply();
                }

                nlb_logo_textView.setText(String.format("%s (%s)", "nlb_logo.tflite", sharedPref.getString("nlb_logo", "Default")));
                red_green_man_textView.setText(String.format("%s (%s)", "red_green_man.tflite", sharedPref.getString("red_green_man", "Default")));
                bus_stop_textView.setText(String.format("%s (%s)", "bus_stop.tflite", sharedPref.getString("bus_stop", "Default")));
            }
        });

        sendImages_switch.setChecked(sharedPref.getBoolean("sendRetrainingImages", true));
        downloadWeights_switch.setChecked(sharedPref.getBoolean("downloadLatestWeights", true));
        landmarkCache_switch.setChecked(sharedPref.getBoolean("enableLandmarkCache", true));

        sendImages_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
                sharedPref.edit().putBoolean("sendRetrainingImages", b).apply();
            }
        });

        downloadWeights_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
                sharedPref.edit().putBoolean("downloadLatestWeights", b).apply();
            }
        });

        landmarkCache_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
                sharedPref.edit().putBoolean("enableLandmarkCache", b).apply();
            }
        });
    }
}