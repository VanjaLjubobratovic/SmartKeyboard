package com.example.smartkeyboard;


import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Voronoi {
    private ArrayList<ArrayList<DoublePoint>> optimalPoints = new ArrayList<>();
    private ArrayList<ArrayList<DoublePoint>> keySizes = new ArrayList<ArrayList<DoublePoint>>();
    private ArrayList<Double> keyHeight = new ArrayList<>();
    private double leftBorder = 0, rightBorder = 150;

    /*public Voronoi(ArrayList<ArrayList<DoublePoint>> optimalPoints){
        this.optimalPoints = optimalPoints;
    }*/
    public Voronoi(){
        for(int i =0 ; i < 4; i++){
            ArrayList<DoublePoint> row = new ArrayList<>();
            optimalPoints.add(row);
        }
        rightBorder = Resources.getSystem().getDisplayMetrics().widthPixels;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void mapToList(LinkedHashMap<String, MistakeModel> map){
        AtomicInteger i = new AtomicInteger(0);
        for (MistakeModel entry : map.values()) {
            DoublePoint key = new DoublePoint();
            key.setX(entry.getCenterX());
            key.setY(entry.getCenterY());
            if(i.get() < 10){
                optimalPoints.get(0).add(key);
            }
            if(i.get() < 19){
                optimalPoints.get(1).add(key);
            }
            if(i.get() < 28){
                optimalPoints.get(2).add(key);
            }
            if(i.get() < 32){
                optimalPoints.get(3).add(key);
            }
            i.getAndIncrement();

            Log.d("CENTROID", entry.toStringCentroid());
        }

    }

    public ArrayList<ArrayList<DoublePoint>> calcWidth(){
        for(int i = 0; i < optimalPoints.size(); i++){
            ArrayList<DoublePoint> row = new ArrayList<DoublePoint>();
            DoublePoint firstKey = new DoublePoint();
            firstKey.setX((optimalPoints.get(i).get(0).getX() + optimalPoints.get(i).get(1).getX()) / 2 - leftBorder);
            firstKey.setY(keyHeight.get(i));
            row.add(firstKey);
            for(int j = 1; j < optimalPoints.get(i).size() - 1; j++){
                DoublePoint key = new DoublePoint();
                key.setX((optimalPoints.get(i).get(j).getX() + optimalPoints.get(i).get(j+1).getX()) / 2 -
                        (optimalPoints.get(i).get(j-1).getX() + optimalPoints.get(i).get(j).getX()) / 2);
                key.setY(keyHeight.get(i));
                row.add(key);
            }
            DoublePoint lastKey = new DoublePoint();
            lastKey.setX(rightBorder - (optimalPoints.get(i).get(optimalPoints.get(i).size() -2).getX()
                    + optimalPoints.get(i).get(optimalPoints.get(i).size() - 1).getX()) / 2);
            lastKey.setY(keyHeight.get(i));
            row.add(lastKey);
            keySizes.add(row);
        }
        Log.d("KEY WIDTH", "Width: " + String.valueOf(keySizes.get(0).get(1).getX()) + "Height: " +
                String.valueOf(keySizes.get(0).get(1).getY()));
    return keySizes;
    }

    public void calcHeight(){
        ArrayList<Double> tmpKeyHeight = new ArrayList<>();
        for(int i = 0; i < optimalPoints.size(); i++) {
            double rowHeight = 0;
            for (int j = 1; j < optimalPoints.get(i).size() - 1; j++) {
                rowHeight += optimalPoints.get(i).get(j).getY();
            }
            rowHeight = rowHeight/optimalPoints.get(i).size();
            tmpKeyHeight.add(rowHeight);
        }

        double topRowHeight = (tmpKeyHeight.get(0) + tmpKeyHeight.get(1)) / 2 - 0;
        keyHeight.add(topRowHeight);
        for(int i = 1;i < tmpKeyHeight.size() - 1; i++){
            double rowHeight =  (tmpKeyHeight.get(i) + tmpKeyHeight.get(i+1)/2) -
                    (tmpKeyHeight.get(i-1) + tmpKeyHeight.get(i))/2;
            keyHeight.add(rowHeight);
        }
        double bottomRowHeight = (650 - tmpKeyHeight.get(tmpKeyHeight.size()-1) +
                tmpKeyHeight.get(tmpKeyHeight.size()-2));
        keyHeight.add(bottomRowHeight);

    }


    public ArrayList<ArrayList<DoublePoint>> getKeySizes() {
        return keySizes;
    }
}
