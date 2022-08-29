package com.example.mymap.Threads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mymap.R;
import com.example.mymap.Utility.HttpRequest;
import com.example.mymap.Utility.Settings;
import com.example.mymap.Utility.UI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class RetrainingThread extends Thread {
    String jwtToken;
    JSONObject response;
    ImageView label_imageView;
    LinearLayout yes_linearLayout;
    LinearLayout no_linearLayout;
    TextView title_textView;
    LinearLayout label_linearLayout;
    Context context;
    public String guid;
    JSONObject colors;

    public RetrainingThread(Context context, String jwtToken, ImageView label_imageView, LinearLayout yes_linearLayout, LinearLayout no_linearLayout, TextView title_textView, LinearLayout label_linearLayout) {
        this.context = context;
        this.jwtToken = jwtToken;
        this.label_imageView = label_imageView;
        this.yes_linearLayout = yes_linearLayout;
        this.no_linearLayout = no_linearLayout;
        this.title_textView = title_textView;
        this.label_linearLayout = label_linearLayout;
    }

    @Override
    public void run() {
        response = HttpRequest.makeGetRequest(Settings.retrainingServer + Settings.getImage, jwtToken);
        if (response == null) {
            return;
        }
        if (!response.has("success")) {
            return;
        }

        try {
            if (response.getBoolean("success")) {
                guid = response.getString("guid");
                String title = response.getString("detectorName");
                colors = response.getJSONObject("colors");
                Log.d("RetrainingThread/run", "colors: " + colors);
                updateImage(response.getString("imageBytes"));
                updateUI(title);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateUI(String title) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title_textView.setText(title);
//                yes_linearLayout.setClickable(true);
//                no_linearLayout.setClickable(true);

                UI.enableLinearLayout(yes_linearLayout);
                UI.enableLinearLayout(no_linearLayout);

                label_linearLayout.removeAllViews();

                Iterator<String> keys = colors.keys();

                // Setting the label legends
                while(keys.hasNext()) {
                    String key = keys.next();
                    try {
                        String rgb = colors.getString(key);
                        Log.d("RetrainingThread/updateUI", "colors: " + rgbToHex(rgb));

                        LinearLayout tempLinearLayout = new LinearLayout(context);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                        tempLinearLayout.setLayoutParams(layoutParams);
                        tempLinearLayout.setGravity(Gravity.CENTER);
                        tempLinearLayout.setOrientation(LinearLayout.VERTICAL);

                        TextView tempTextView = new TextView(context);
                        tempTextView.setText(key);
                        tempTextView.setGravity(Gravity.CENTER);
                        tempTextView.setTextColor(Color.BLACK);

                        ImageView tempImageView = new ImageView(context);
                        layoutParams = new LinearLayout.LayoutParams(170, 45);
                        layoutParams.setMargins(0, 15, 0, 0);
                        tempImageView.setLayoutParams(layoutParams);

                        tempImageView.setBackgroundColor(Color.parseColor(rgbToHex(rgb)));

                        tempLinearLayout.addView(tempTextView);
                        tempLinearLayout.addView(tempImageView);
                        label_linearLayout.addView(tempLinearLayout);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

//                <LinearLayout-->
//<!--                android:layout_width="0dp"-->
//<!--                android:layout_height="match_parent"-->
//<!--                android:layout_weight="1">-->
//<!--                <TextView-->
//<!--                    android:id="@+id/textview1"-->
//<!--                    android:layout_width="70dp"-->
//<!--                    android:layout_height="80dp"-->
//<!--                    app:layout_flexBasisPercent="50%"-->
//<!--                    android:text="TEST1"-->
//<!--                />-->
//<!--            </LinearLayout>-->

    public void updateImage(String b64String) {
        byte[] decodedString = Base64.decode(b64String, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label_imageView.setImageBitmap(decodedByte);
            }
        });
    }

    public String rgbToHex(String rgb) {
        StringBuilder sb = new StringBuilder(rgb);
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(0);
        String[] intArr;
        intArr = sb.toString().split(",");
        return "#" + Integer.toHexString(Integer.parseInt(intArr[0])) + Integer.toHexString(Integer.parseInt(intArr[1].substring(1))) + Integer.toHexString(Integer.parseInt(intArr[2].substring(1)));
    }
}
