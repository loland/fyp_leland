package com.example.mymap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mymap.Utility.HttpRequest;
import com.example.mymap.Utility.ParameterStringBuilder;
import com.example.mymap.Utility.Retraining;
import com.example.mymap.Utility.Settings;
import com.example.mymap.Utility.UI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    TextView header_textView;
    EditText username_editText;
    EditText password_editText;
    EditText retype_password_editText;
    Button submit_button;
    TextView register_textView;
    boolean register;
    LinearLayout retype_password_linearLayout;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        header_textView = findViewById(R.id.header_textView);
        username_editText = findViewById(R.id.username_editText);
        password_editText = findViewById(R.id.password_editText);
        retype_password_editText = findViewById(R.id.retype_password_editText);
        submit_button = findViewById(R.id.submit_button);
        register_textView = findViewById(R.id.register_textView);
        retype_password_linearLayout = findViewById(R.id.retype_password_linearLayout);

        register = false;

        register_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (register) {
                    register = false;
                    header_textView.setText(R.string.login);
                    retype_password_linearLayout.animate().alpha(0.0f);
                    retype_password_linearLayout.setVisibility(View.GONE);
                    register_textView.setText(R.string.don_t_have_an_account);

                } else {
                    register = true;
                    header_textView.setText(R.string.register);
                    retype_password_linearLayout.setVisibility(View.VISIBLE);
                    retype_password_linearLayout.animate().alpha(1.0f);
                    register_textView.setText(R.string.back_to_login);
                }
            }
        });

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = username_editText.getText().toString();
                String password = password_editText.getText().toString();

                if (username.length() == 0) {
                    return;
                }
                if (password.length() == 0) {
                    return;
                }

                UI.disableButton(submit_button);

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("username", username);
                map.put("password", password);
                String json = ParameterStringBuilder.getJsonFromHashmap(map);

                JSONObject response;
                if (register) {
                    String retypedPassword = retype_password_editText.getText().toString();
                    if (retypedPassword.length() == 0) {
                        UI.enableButton(submit_button);
                        return;
                    }
                    if (!password.equals(retypedPassword)) {
                        UI.enableButton(submit_button);
                        return;
                    }
                    response = HttpRequest.makePostRequest(json, Settings.retrainingServer + Settings.register, 3);

                } else {
                    response = HttpRequest.makePostRequest(json, Settings.retrainingServer + Settings.login, 3);
                }

                try {
                    if (response != null) {
                        if (response.getBoolean("success")) {
                            String token = response.getString("token");
                            Retraining.storeToken(token, context);
                            Intent intent = new Intent(context, LabelActivity.class);// New activity
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(intent);
                            ((Activity)context).finish();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    UI.enableButton(submit_button);
                }
            }
        });
    }
}