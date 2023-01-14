package com.example.smartkeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;

public class CustomKeyboard extends Keyboard {
    public CustomKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    @Override
    public int getHeight() {
        return getKeyHeight() * 4;
    }

    public void changeKeyHeight(double height_modifier)
    {
        int height = 0;
        for(Keyboard.Key key : getKeys()) {
            key.height *= height_modifier;
            key.y *= height_modifier;
            height = key.height;
        }
        setKeyHeight(height);
        getNearestKeys(0, 0); //somehow adding this fixed a weird bug where bottom row keys could not be pressed if keyboard height is too tall.. from the Keyboard source code seems like calling this will recalculate some values used in keypress detection calculation
    }
}
