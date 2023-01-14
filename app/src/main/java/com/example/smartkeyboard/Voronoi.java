package com.example.smartkeyboard;


import android.graphics.Point;

import java.util.ArrayList;

public class Voronoi {
    private ArrayList<ArrayList<DoublePoint>> optimalPoints;
    private ArrayList<ArrayList<DoublePoint>> keySizes = new ArrayList<ArrayList<DoublePoint>>();
    private double leftBorder = 0, rightBorder = 150;
    public Voronoi(ArrayList<ArrayList<DoublePoint>> optimalPoints){
        this.optimalPoints = optimalPoints;
    }


    public ArrayList<ArrayList<DoublePoint>> calcWidth(){
        for(int i = 0; i < optimalPoints.size(); i++){
            ArrayList<DoublePoint> row = new ArrayList<DoublePoint>();
            DoublePoint firstKey = new DoublePoint();
            firstKey.setX((optimalPoints.get(i).get(0).getX() + optimalPoints.get(i).get(1).getX()) / 2 - leftBorder);
            row.add(firstKey);
            for(int j = 1; j < optimalPoints.get(i).size() - 1; j++){
                DoublePoint key = new DoublePoint();
                key.setX((optimalPoints.get(i).get(j).getX() + optimalPoints.get(i).get(j+1).getX()) / 2 -
                        (optimalPoints.get(i).get(j-1).getX() + optimalPoints.get(i).get(j).getX()) / 2);
                row.add(key);
            }
            DoublePoint lastKey = new DoublePoint();
            lastKey.setX(rightBorder - (optimalPoints.get(i).get(optimalPoints.get(i).size() -2).getX()
                    + optimalPoints.get(i).get(optimalPoints.get(i).size() - 1).getX()) / 2);
            row.add(lastKey);
            keySizes.add(row);
        }
    return keySizes;
    }

    public ArrayList<ArrayList<DoublePoint>> getKeySizes() {
        return keySizes;
    }
}
