package com.example.mymap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mymap.Threads.RetrainingThread;
import com.example.mymap.Utility.HttpRequest;
import com.example.mymap.Utility.Retraining;
import com.example.mymap.Utility.Settings;
import com.example.mymap.Utility.UI;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public class LabelActivity extends AppCompatActivity {
    Context context;
    ImageView label_imageView;
    LinearLayout yes_linearLayout;
    LinearLayout no_linearLayout;
    TextView title_textView;
    LinearLayout label_linearLayout;
    String detectorName;
    String colors;
    String jwtToken;
    RetrainingThread retrainingThread;
    TextView logout_textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        jwtToken = Retraining.getToken(this);

        if (jwtToken == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);

        label_imageView = findViewById(R.id.label_imageView);
        yes_linearLayout = findViewById(R.id.yes_linearLayout);
        no_linearLayout = findViewById(R.id.no_linearLayout);
        title_textView = findViewById(R.id.title_textView);
        label_linearLayout = findViewById(R.id.label_linearLayout);
        logout_textView = findViewById(R.id.logout_textView);

        retrainingThread = new RetrainingThread(this, jwtToken, label_imageView, yes_linearLayout, no_linearLayout, title_textView, label_linearLayout);
        retrainingThread.start();

        yes_linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UI.disableLinearLayout(yes_linearLayout);
                UI.disableLinearLayout(no_linearLayout);
                setLoadingImage();

                Retraining.submitResult(retrainingThread.guid, true, jwtToken);

                retrainingThread = new RetrainingThread(context, jwtToken, label_imageView, yes_linearLayout, no_linearLayout, title_textView, label_linearLayout);
                retrainingThread.start();
            }
        });

        no_linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UI.enableLinearLayout(yes_linearLayout);
                UI.enableLinearLayout(no_linearLayout);
                setLoadingImage();

                Retraining.submitResult(retrainingThread.guid, false, jwtToken);

                retrainingThread = new RetrainingThread(context, jwtToken, label_imageView, yes_linearLayout, no_linearLayout, title_textView, label_linearLayout);
                retrainingThread.start();
            }
        });

        logout_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Retraining.removeToken(context);
                finish();
            }
        });
    }

    public void setLoadingImage() {
        label_imageView.setImageResource(R.drawable.loading);
    }

}