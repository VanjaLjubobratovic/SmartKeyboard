package com.example.smartkeyboard;

import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

public class SmartInputService extends InputMethodService implements KeyboardView.OnKeyboardActionListener, View.OnTouchListener {
    public static final String KEYBOARD_TOUCH = "KeyboardTouched";

    private KeyboardView keyboardView;
    private Keyboard keyboard;

    private boolean caps = false;

    private ArrayList<KeyModel> keys;

    @Override
    public View onCreateInputView() {
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboard = new Keyboard(this, R.xml.keys_layout);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        keyboardView.setOnTouchListener(this);

        return keyboardView;
    }

    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection inputConnection = getCurrentInputConnection();

        //Coordinates test
        Keyboard.Key pressedKey = findKey(primaryCode);

        Log.d("PROPERTIES", "Xmax: " + keyboardView.getWidth() + "\n"
                                + "Ymax: " + keyboardView.getHeight() + "\n");



        if (inputConnection != null) {
            switch (primaryCode) {
                case Keyboard.KEYCODE_DELETE:
                    CharSequence selectedText = inputConnection.getSelectedText(0);

                    if (TextUtils.isEmpty(selectedText)) {
                        inputConnection.deleteSurroundingText(1, 0);
                    } else {
                        inputConnection.commitText("", 1);
                    }
                    break;

                case Keyboard.KEYCODE_SHIFT:
                    caps = !caps;
                    keyboard.setShifted(caps);
                    keyboardView.invalidateAllKeys();
                    break;

                case Keyboard.KEYCODE_DONE:
                    inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    break;

                default:
                    char code = (char) primaryCode;
                    if (Character.isLetter(code) && caps) {
                        code = Character.toUpperCase(code);
                    }
                    inputConnection.commitText(String.valueOf(code), 1);
            }
        }
    }



    private Keyboard.Key findKey(int code) {
        List<Keyboard.Key> keys = this.keyboard.getKeys();

        for(Keyboard.Key k : keys) {
            if (k.codes[0] == code) {
                /*int index = keys.indexOf(k);

                k.width += 20;
                k.x -= 10;

                if(index != 0) {
                    keys.get(index - 1).width -= 10;
                }
                if (index != keys.size() - 1) {
                    keys.get(index + 1).width -= 10;
                    keys.get(index + 1).x += 10;
                }
                keyboardView.invalidateAllKeys();*/
                return k;
            }
        }
        return null;
    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }


    private void broadcastTouch(int x, int y) {
        Intent intent = new Intent(SmartInputService.KEYBOARD_TOUCH);
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        sendBroadcast(intent);
        //LocalBroadcastManager.getInstance(SmartInputService.this).sendBroadcast(intent);
        Log.d("TOUCH_BROADCAST", "SENT!");
    }

    public Keyboard getKeyboard() {
        return this.keyboard;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            Log.d("TOUCH", "pressX: " + motionEvent.getX() + "\n" +
                    "pressY: " + motionEvent.getY());

            broadcastTouch((int) motionEvent.getX(), (int) motionEvent.getY());
        }
        return false;
    }
}
