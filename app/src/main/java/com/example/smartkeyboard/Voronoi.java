package com.example.smartkeyboard;


import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Voronoi {
    private ArrayList<ArrayList<DoublePoint>> optimalPoints;
    private ArrayList<ArrayList<DoublePoint>> keySizes;
    private ArrayList<Double> keyHeight;
    private double leftBorder = 0, rightBorder = 150;

    /*public Voronoi(ArrayList<ArrayList<DoublePoint>> optimalPoints){
        this.optimalPoints = optimalPoints;
    }*/
    public Voronoi(){
        optimalPoints = new ArrayList<>();
        keySizes = new ArrayList<>();
        keyHeight = new ArrayList<>();
        for(int i =0 ; i < 4; i++){
            ArrayList<DoublePoint> row = new ArrayList<>();
            optimalPoints.add(row);
        }
        rightBorder = Resources.getSystem().getDisplayMetrics().widthPixels;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void mapToList(LinkedHashMap<String, MistakeModel> map){
        int i = 0;
        for (MistakeModel entry : map.values()) {
            DoublePoint key = new DoublePoint(entry.getCentroidX(), entry.getCentroidY());
            if(i < 10){
                optimalPoints.get(0).add(key);
            }else if(i < 19){
                optimalPoints.get(1).add(key);
            } else if(i < 28){
                optimalPoints.get(2).add(key);
            } else if(i < 32){
                optimalPoints.get(3).add(key);
            }

            i++;

            Log.d("MISTAKE", entry.toString());
            Log.d("MISTAKE", key.getX() + "; " + key.getY());
        }

        System.out.println("ROW SIZE: " + optimalPoints.get(3).size());

    }

    public ArrayList<ArrayList<DoublePoint>> calcWidth(){
        /*for(int i = 0; i < optimalPoints.size(); i++){
            ArrayList<DoublePoint> row = new ArrayList<>();
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
        }*/

        for(ArrayList<DoublePoint> row : optimalPoints) {
            ArrayList<DoublePoint> rowSizes = new ArrayList<>();
            for(DoublePoint key : row) {
                int index = row.indexOf(key);
                System.out.println("KEY SIZE |||| " + key.getX() + ";" + key.getY() + " INDEX: " + index);
                if(index != 0 && index != row.size() - 1) {
                    int left = (int)(key.getX() - row.get(index - 1).getX()) / 2;
                    int right = (int)(row.get(index + 1).getX() - key.getX()) / 2;

                    rowSizes.add(new DoublePoint(left + right, 150));
                } else if (index == 0) {
                    int right = (int)(row.get(index + 1).getX() - key.getX()) / 2;

                    rowSizes.add(new DoublePoint(right * 2, 150));
                } else {
                    int left = (int)(key.getX() - row.get(index - 1).getX()) / 2;

                    rowSizes.add(new DoublePoint(left * 2, 150));
                }

                System.out.println("KEY SIZE: " + rowSizes.get(rowSizes.size() - 1).getX());
            }
            keySizes.add(rowSizes);
        }
        /*Log.d("KEY WIDTH", "Width: " + String.valueOf(keySizes.get(0).get(1).getX()) + "Height: " +
                String.valueOf(keySizes.get(0).get(1).getY()));*/
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
