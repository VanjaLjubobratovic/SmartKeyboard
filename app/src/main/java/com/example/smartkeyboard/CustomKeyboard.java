package com.example.smartkeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CustomKeyboard extends Keyboard {
    public CustomKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    @Override
    public int getHeight() {
        //30px for spacing between rows
        return getKeyHeight() * 4 + 30;
    }

    public void changeKeyHeight()
    {
        int height = (getKeys().get(9).height + getKeys().get(18).height + getKeys().get(27).height + getKeys().get(28).height) / 4 + 10;
        setKeyHeight(height);

        /*somehow adding this fixed a weird bug where bottom row keys could not be pressed if keyboard height is too tall..
        from the Keyboard source code seems like calling this will recalculate some values used in keypress detection calculation*/
        getNearestKeys(0, 0);
    }
}
