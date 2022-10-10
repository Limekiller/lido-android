package com.bryceyoder.lido;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;

public class LoginActivity extends Activity {
    
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

        // Otherwise, attach a listener to the keyboard
        EditText text = findViewById(R.id.editTextDomain);
        text.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                connect(null);
            }
            return false;
        });
    }

    /**
     * Given a domain, pass it to the main activity and go there
     * @param domain The domain of the Lido instance
     */
    public void connect(String domain) {
        // If a null value is passed, grab the value of the domain editText
        if (domain == null) {
            EditText text = findViewById(R.id.editTextDomain);
            domain = String.valueOf(text.getText());
        }

        // Save the domain
        SharedPreferences.Editor editor = this.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString("domain", domain);
        editor.apply();

        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("domain", domain);
        startActivity(i);
    }

}
