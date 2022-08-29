package com.example.mymap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.mymap.Utility.FileHelper;
import com.example.mymap.Utility.Settings;
import com.example.mymap.tflite.DetectorFactory;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView ui_imageView = findViewById(R.id.ui_imageView);
        ImageView audio_imageView = findViewById(R.id.audio_imageView);
        ImageView camera_imageView = findViewById(R.id.camera_imageView);
        ImageView label_imageView = findViewById(R.id.label_imageView);
        ImageView about_imageView = findViewById(R.id.about_imageView);

        Context context = this;

        ui_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HomeUIActivity.class);
                startActivity(intent);
            }
        });

        audio_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HomeActivity.class);
                startActivity(intent);
            }
        });

        camera_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetectorActivity.class);
                startActivity(intent);
            }
        });

        label_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LabelActivity.class);
                startActivity(intent);
            }
        });

        about_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AboutActivity.class);
                startActivity(intent);
            }
        });


        SharedPreferences sharedPref = context.getSharedPreferences(Settings.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
//        sharedPref.edit().putString("red_green_man", "0").apply();
//        sharedPref.edit().putString("nlb_logo", "0").apply();
//        sharedPref.edit().putString("bus_stop", "0").apply();

//        File file = new File(context.getFilesDir(), "red_green_man.tflite");
//        Log.d("MainActivity/onCreate", file + " " + String.valueOf(file.exists()));

        if (sharedPref.getBoolean("downloadLatestWeights", true)) {
            Thread updateModelThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    DetectorFactory.requestModelUpdates(context);
                }
            });
            updateModelThread.start();
        }
    }
}