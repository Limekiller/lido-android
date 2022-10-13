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
    private volatile String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if Lido domain is saved -- if so, go straight there
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String domain = sharedPref.getString("domain", null);
        if (domain != null) {
            connect(domain, null);
        }

        // Otherwise, attach a listener to the password text
        EditText password = findViewById(R.id.editTextTextPassword);
        password.setOnKeyListener((v, keyCode, event) -> {
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
        EditText password = findViewById(R.id.editTextTextPassword);
        String passwordText = String.valueOf(password.getText());

        if (!domainText.trim().isEmpty() && !passwordText.trim().isEmpty()) {
            this.domain = String.valueOf(text.getText());
            this.password = passwordText;
            postLogin();
        }
    }

    /**
     * Start the login thread
     */
    private void postLogin() {
        LoginThread loginThread = new LoginThread(this.domain, this.password);
        Thread thread = new Thread(loginThread);
        thread.start();

        try {
            thread.join();
            String threadError = loginThread.getError();
            String sessionToken = loginThread.getSessionToken();
            if (threadError == null && sessionToken != null) {
                connect(this.domain, sessionToken);
            } else {
                Toast.makeText(this, threadError, Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong with the login process.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Given a domain and token, pass it to the main activity and go there
     * @param domain The domain of the Lido instance
     * @param sessionToken The session token from the login process
     */
    public void connect(String domain, String sessionToken) {
        // Save the domain
        SharedPreferences.Editor editor = this.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString("domain", domain);
        editor.apply();

        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("domain", domain);
        i.putExtra("sessionToken", sessionToken);
        startActivity(i);
    }
}

class LoginThread extends Thread implements Runnable {

    private final String domain;
    private final String password;

    private volatile List<String> cookies;
    private volatile String csrfToken;
    private volatile String sessionToken;
    private volatile String error;

    LoginThread(String domain, String password) {
        this.domain = domain;
        this.password = password;
    }

    public String getSessionToken() {
        return sessionToken;
    }
    public String getError() {
        return error;
    }

    /**
     * Get the initial CSRF token
     */
    private void getCSRFToken() {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url("https://" + domain + "/api/auth/csrf")
                .build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject respJSON = new JSONObject(response.body().string());
            this.csrfToken = respJSON.get("csrfToken").toString();
            this.cookies = response.headers("Set-Cookie");
            response.body().close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            this.error = "Failed to get CSRF token";
        }
    }

    /**
     * Once a CSRF token is set, make a request to the login page to try to get session token
     */
    private void postLoginAndGetSessionToken() {

        StringBuilder cookieString = new StringBuilder();
        for(String cookie:cookies) {
            cookieString.append(cookie.split(";")[0]);
            cookieString.append("; ");
        }

        OkHttpClient client = new OkHttpClient().newBuilder().followRedirects(false).build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "csrfToken=" + csrfToken + "&password=" + this.password);
        Request request = new Request.Builder()
                .url("https://" + domain + "/api/auth/callback/credentials")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Cookie", cookieString.substring(0, cookieString.length() - 2))
                .build();
        try {
            Response response = client.newCall(request).execute();
            for(String cookie:response.headers("Set-Cookie")) {
                if (cookie.split("=")[0].equals("__Secure-next-auth.session-token")) {
                    this.sessionToken = cookie.split(";")[0].split("=")[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.error = "Failed to get session token";
        }
    }

    @Override
    public void run() {
        getCSRFToken();
        if (this.error == null) {
            postLoginAndGetSessionToken();
            if (this.sessionToken == null) {
                this.error = "Invalid domain or password";
            }
        }
    }
}
