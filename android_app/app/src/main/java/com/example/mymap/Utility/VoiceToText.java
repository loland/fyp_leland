package com.example.mymap.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mymap.HomeActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class VoiceToText extends HomeActivity {
    public Context context;
    public VoiceToText(Context context) {
        this.context = context;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

        try {
            ((Activity) context).startActivityForResult(intent, 1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
