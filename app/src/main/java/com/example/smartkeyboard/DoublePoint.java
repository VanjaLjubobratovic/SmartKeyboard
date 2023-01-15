package com.example.smartkeyboard;

import android.os.Parcel;
import android.os.Parcelable;

public class DoublePoint implements Parcelable {
    private double x,y;

    protected DoublePoint(Parcel in) {
        x = in.readDouble();
        y = in.readDouble();
    }

    public static final Creator<DoublePoint> CREATOR = new Creator<DoublePoint>() {
        @Override
        public DoublePoint createFromParcel(Parcel in) {
            return new DoublePoint(in);
        }

        @Override
        public DoublePoint[] newArray(int size) {
            return new DoublePoint[size];
        }
    };

    public DoublePoint(){
    }

    public DoublePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeDouble(x);
        parcel.writeDouble(y);
    }
}
