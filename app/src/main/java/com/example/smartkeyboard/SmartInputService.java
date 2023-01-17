package com.example.smartkeyboard;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SmartInputService extends InputMethodService implements KeyboardView.OnKeyboardActionListener, View.OnTouchListener {
    public static final String KEYBOARD_TOUCH = "KeyboardTouched";
    public static final String KEYBOARD_SETTINGS = "KeyboardSettings";

    private KeyboardView keyboardView;
    private CustomKeyboard keyboard;

    private boolean caps = false;

    @Override
    public View onCreateInputView() {
        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        //keyboard = new CustomKeyboard(this, R.xml.keys_layout);

        //Read from config
        keyboard = SmartInputService.readKeyboardConfig(getApplicationContext());
        keyboardView.setKeyboard(keyboard);
        keyboardView.setPreviewEnabled(false);
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
                    /*caps = !caps;
                    keyboard.setShifted(caps);
                    keyboardView.invalidateAllKeys();*/
                    calibrateFromConfig();
                    break;

                case Keyboard.KEYCODE_DONE:
                    inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    break;
                default:
                    char code = (char) primaryCode;
                    if (Character.isLetter(code) && caps) {
                        code = Character.toUpperCase(code);
                    }
                    switch(code) {
                        case 49:
                            keyboardView.setKeyboard(keyboard);
                            break;
                        case 50:
                            //TODO pokreni kalbraciju
                            calibrateFromConfig();
                            break;
                        case 51:
                            //TODO resetiraj kalibraciju
                            resetConfig();
                            calibrateFromConfig();
                            break;
                    }
                    if(code != 49 && code !=50 && code != 51)
                        inputConnection.commitText(String.valueOf(code), 1);
            }
        }
    }


    private void resetConfig() {
        String filename = "keyboardConfig.txt";
        File file = new File(getApplicationContext().getFilesDir(), filename);
        if(file.delete()) {
            System.out.println("File deleted");
        }
    }

    private void calibrateFromConfig() {
        this.keyboard = SmartInputService.readKeyboardConfig(getApplicationContext());

        keyboardView.setKeyboard(keyboard);
        keyboardView.closing();
    }

    public static CustomKeyboard readKeyboardConfig(Context c) {
        CustomKeyboard keyboard = new CustomKeyboard(c.getApplicationContext(), R.xml.keys_layout);

        try {
            File input = new File(c.getFilesDir().getAbsolutePath());
            BufferedReader bufferedReader = new BufferedReader(new FileReader(input + "/keyboardConfig.txt"));

            int i = 0;
            String line = bufferedReader.readLine();

            while (line != null) {
                String[] coords = line.split(";");
                int x = Math.abs(Integer.parseInt(coords[0]));
                int y = Math.abs(Integer.parseInt(coords[1]));
                System.out.println(line + " || " + x + " " + y);
                Keyboard.Key k = keyboard.getKeys().get(i);

                int difference = x - k.width;

                if(isLeftEdge(i)) {
                    if(difference < 0) {
                        k.x -= difference / 2;
                    }

                } else {
                    k.x = keyboard.getKeys().get(i - 1).x + keyboard.getKeys().get(i - 1).width;
                }

                k.width = x;
                k.height = y;

                int lookupIndex = 0;
                if(i > 27) {
                    lookupIndex = 27;
                } else if(i > 18) {
                    lookupIndex = 18;
                } else if (i > 9) {
                    lookupIndex = 9;
                } else {
                    i++;
                    line = bufferedReader.readLine();
                    continue;
                }

                k.y = keyboard.getKeys().get(lookupIndex).height + keyboard.getKeys().get(lookupIndex).y;
                i++;
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        keyboard.changeKeyHeight();
        return keyboard;
    }

    private boolean isRightEdge(int index) {
        return (index == 9 || index == 18 || index == 27 || index == 31);
    }

    private static boolean isLeftEdge(int index) {
        return (index == 0 || index == 10 || index == 19 || index == 28);
    }


    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {
        Keyboard settings = new Keyboard(this, R.xml.settings_keys);
        keyboardView.setKeyboard(settings);
    }

    @Override
    public void swipeRight() {
        Keyboard settings = new Keyboard(this, R.xml.settings_keys);
        keyboardView.setKeyboard(settings);
    }

    @Override
    public void swipeDown() {
        Keyboard settings = new Keyboard(this, R.xml.settings_keys);
        keyboardView.setKeyboard(settings);
    }

    @Override
    public void swipeUp() {
        Keyboard settings = new Keyboard(this, R.xml.settings_keys);
        keyboardView.setKeyboard(settings);
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
