package com.example.smartkeyboard;

import android.inputmethodservice.Keyboard;

public class KeyModel {
    private Keyboard.Key key;
    private double errX, errY;


    public Keyboard.Key getKey() {
        return key;
    }

    public void setKey(Keyboard.Key key) {
        this.key = key;
    }

    public double getErrX() {
        return errX;
    }

    public void setErrX(double errX) {
        this.errX = errX;
    }

    public double getErrY() {
        return errY;
    }

    public void setErrY(double errY) {
        this.errY = errY;
    }
}
