package com.example.mymap.Utility;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import com.example.mymap.General.RoutingSettings;

import java.util.Locale;
import java.util.Map;

public class VoiceSpeaker {
    static public TextToSpeech ttsObj;
    static public boolean audio = true;
    public VoiceSpeaker(Context context) {
        ttsObj = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        });
        ttsObj.setLanguage(Locale.US);
    }

    public VoiceSpeaker(Context context, TextToSpeech.OnInitListener listener) {
        ttsObj = new TextToSpeech(context, listener);
        ttsObj.setLanguage(Locale.US);
    }

    public void say(String speech) {
        if (audio) {
            ttsObj.speak(speech, TextToSpeech.QUEUE_FLUSH,null,null);;
        }
    }

    public void sayWithoutFlush(String speech) {
        if (audio) {
            ttsObj.speak(speech, TextToSpeech.QUEUE_ADD,null,null);;
        }
    }

}
