package com.bryceyoder.lido;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LidoWebView extends WebView {
    public LidoWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * I ALREADY did tons of work implementing spatial nav in Lido via the keyboard,
     * and there's a bunch of edge-cases that I specifically programmed for that
     * the basic, ootb AndroidTV spatial nav can't handle. So here we're intercepting
     * any remote presses and translating them into simulated keypresses in JS
     *
     * @param event The KeyEvent that triggers the method
     * @return boolean True to cancel the original remote event, false to allow it through
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        final int keyAction = event.getAction();

        // Mapping AndroidTV remote keyCodes to JavaScript keyCodes
        Map<Integer, String[]> keyCodeList = new HashMap<>();
        keyCodeList.put(19, new String[] {"38", "ArrowUp"}); // up
        keyCodeList.put(22, new String[] {"39", "ArrowRight"}); // right
        keyCodeList.put(20, new String[] {"40", "ArrowDown"}); // down
        keyCodeList.put(21, new String[] {"37", "ArrowLeft"}); // left
        keyCodeList.put(23, new String[] {"13", "Enter"}); // enter
        keyCodeList.put(4, new String[] {"0", "None"}); // back (but don't do anything)

        if (keyAction == KeyEvent.ACTION_DOWN) {

            // Whoo boy, the enter button is tricky.
            // For links, we have to call .click()
            // But this doesn't exist on buttons and divs, so we dispatch an event
            // But in SOME cases, the active element isn't the one with the listener --
            // the child has the listener -- so we also check that case
            // and dispatch the listener on the child if need be
            // Finally, we fire one more event with the "Enter" code as some things listen for that instead.
            if (Objects.equals(keyCodeList.get(keyCode)[0], "13")) {
                loadUrl(
                    // (First, we create a reusable keyboard event for this if it doesn't exist)
                    "javascript:if (typeof mEvent === 'undefined') {" +
                        "var mEvent = document.createEvent('MouseEvents');" +
                        "mEvent.initEvent('click', true, true);" +
                    "}" +
                    "if (document.activeElement.children[0]" +
                            "&& typeof document.activeElement.children[0].click === 'function') {" +
                        "document.activeElement.children[0].click();" +
                    "} else if (typeof document.activeElement.click === 'function'){" +
                        "document.activeElement.click();" +
                    "} else {" +
                        "document.activeElement.dispatchEvent(mEvent);" +
                    "}" +
                    "document.dispatchEvent(new KeyboardEvent('keydown', {" +
                        "keyCode: " + keyCodeList.get(keyCode)[0] + "," +
                        "key: " + keyCodeList.get(keyCode)[0] + "," +
                        "code: '" + keyCodeList.get(keyCode)[1] + "'," +
                        "bubbles: true" +
                    "}))"
                );
                return true;
            }

            // If it's not the enter button, easy -- just dispatch an event for it
            loadUrl(
                "javascript:document.dispatchEvent(new KeyboardEvent('keydown', {" +
                    "keyCode: " + keyCodeList.get(keyCode)[0] + "," +
                    "key: " + keyCodeList.get(keyCode)[0] + "," +
                    "code: '" + keyCodeList.get(keyCode)[1] + "'," +
                    "bubbles: true" +
                "}))"
            );
        } else if (keyAction == KeyEvent.ACTION_UP) {
            if (checkOrFireBackEvent(keyCode)) {
                return true;
            };

            loadUrl(
                "javascript:document.dispatchEvent(new KeyboardEvent('keyup', {" +
                    "keyCode: " + keyCodeList.get(keyCode)[0] + "," +
                    "key: " + keyCodeList.get(keyCode)[0] + "," +
                    "code: '" + keyCodeList.get(keyCode)[1] + "'," +
                    "bubbles: true" +
                "}))"
            );
        }

        return true;
    }

    private boolean checkOrFireBackEvent(int keyCode) {
        if (keyCode == 4) {
            if (this.canGoBack()) {
                this.goBack();
            }
            return true;
        }
        return false;
    }
}
