package com.example.mymap.Utility;

import android.widget.Button;
import android.widget.LinearLayout;

public class UI {
    public static void enableButton(Button button) {
        button.setEnabled(true);
        button.setAlpha(1f);
    }

    public static void disableButton(Button button) {
        button.setEnabled(false);
        button.setAlpha(0.5f);
    }

    public static void enableLinearLayout(LinearLayout linearLayout) {
        linearLayout.setEnabled(true);
        linearLayout.setAlpha(1f);
        linearLayout.setClickable(true);
    }

    public static void disableLinearLayout(LinearLayout linearLayout) {
        linearLayout.setEnabled(false);
        linearLayout.setAlpha(0.5f);
        linearLayout.setClickable(false);
    }
}
