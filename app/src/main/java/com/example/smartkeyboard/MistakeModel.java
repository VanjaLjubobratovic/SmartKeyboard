package com.example.smartkeyboard;

import android.inputmethodservice.Keyboard;

import androidx.annotation.NonNull;

public class MistakeModel {
    private Keyboard.Key key;
    private Integer mistakeX, mistakeY;
    private Integer totalMistakes;

    public MistakeModel(Keyboard.Key key, Integer mistakeX, Integer mistakeY) {
        this.key = key;
        this.mistakeX = mistakeX;
        this.mistakeY = mistakeY;
        this.totalMistakes = 0;
    }

    public MistakeModel(Keyboard.Key key) {
        this.key = key;
        this.mistakeX = 0;
        this.mistakeY = 0;
        this.totalMistakes = 0;
    }

    public MistakeModel() {
    }

    public Keyboard.Key getKey() {
        return key;
    }

    public void setKey(Keyboard.Key key) {
        this.key = key;
    }

    public Integer getMistakeX() {
        return mistakeX;
    }

    public void setMistakeX(Integer mistakeX) {
        this.mistakeX = mistakeX;
    }

    public Integer getMistakeY() {
        return mistakeY;
    }

    public void setMistakeY(Integer mistakeY) {
        this.mistakeY = mistakeY;
    }

    public int getCenterX() {
        return this.key.x + this.key.width / 2;
    }

    public int getCenterY() {
        return this.key.y + this.key.height / 2;
    }

    public Integer getTotalMistakes() {
        return totalMistakes;
    }

    public void setTotalMistakes(Integer totalMistakes) {
        this.totalMistakes = totalMistakes;
    }

    public void addMistakes(Integer mistakeX, Integer mistakeY) {
        this.mistakeY += mistakeY;
        this.mistakeX += mistakeX;
        this.totalMistakes++;
    }

    public void calculateAvgs() {
        if (totalMistakes == 0)
            return;

        this.mistakeX /= totalMistakes;
        this.mistakeY /= totalMistakes;
    }

    @NonNull
    @Override
    public String toString() {
        return "KEY: " + key.label + "; missX: " + mistakeX + "; missY: " + mistakeY + "\n";
    }
}
