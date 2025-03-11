package com.bryceyoder.lido;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginActivity extends Activity {

    private volatile String domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if Lido domain is saved -- if so, go straight there
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String domain = sharedPref.getString("domain", null);
        if (domain != null) {
            connect(domain);
        }

        // Otherwise, attach a listener to the domain text
        EditText textDomain = findViewById(R.id.editTextDomain);
        textDomain.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                onClick(null);
            }
            return false;
        });
    }

    /**
     * This function fires when the form is submitted, activating the login flow
     * @param view Required by API
     */
    public void onClick(View view) {
        EditText text = findViewById(R.id.editTextDomain);
        String domainText = String.valueOf(text.getText());

        if (!domainText.trim().isEmpty()) {
            this.domain = String.valueOf(text.getText());
            connect(this.domain);
        }
    }

    /**
     * Given a domain and token, pass it to the main activity and go there
     * @param domain The domain of the Lido instance
     */
    public void connect(String domain) {
        // Save the domain
        SharedPreferences.Editor editor = this.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString("domain", domain);
        editor.apply();

        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("domain", domain);
        startActivity(i);
    }
}
