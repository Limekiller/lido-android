package com.bryceyoder.lido;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

import java.util.HashMap;
import java.util.Map;

public class LidoWebView extends WebView {
    public LidoWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        final int keyAction = event.getAction();

        Map<Integer, String> keyCodeList = new HashMap<Integer, String>();
        keyCodeList.put(19, "38"); // up
        keyCodeList.put(22, "39"); // right
        keyCodeList.put(20, "40"); // down
        keyCodeList.put(21, "37"); // left
        keyCodeList.put(23, "Enter"); // enter

        Log.d("keycode", String.valueOf(keyCode));

        if (keyAction == KeyEvent.ACTION_DOWN) {
            loadUrl(
                "javascript:window.dispatchEvent(new KeyboardEvent('keydown', {" +
                    "keyCode: " + keyCodeList.get(keyCode) + "," +
                    "key: " + keyCodeList.get(keyCode) + "," +
                    "bubbles: true" +
                "}))"
            );
        } else if (keyAction == KeyEvent.ACTION_UP) {
            loadUrl(
                "javascript:window.dispatchEvent(new KeyboardEvent('keyup', {" +
                    "keyCode: " + keyCodeList.get(keyCode) + "," +
                    "key: " + keyCodeList.get(keyCode) + "," +
                    "bubbles: true" +
                "}))"
            );
        }
        return true;
    }
}
