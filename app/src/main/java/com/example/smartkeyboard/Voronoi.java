package com.example.smartkeyboard;


import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class Voronoi {
    public static final double WIDTH_SHRINK_COEF = 0.15;
    public static final double WIDTH_EXPAND_COEF = 0.60;
    public static final double HEIGHT_EXPAND_COEF = 0.25;
    public static final double MIN_NUM_OF_MISTAKES = 2;

    private ArrayList<ArrayList<MistakeModel>> optimalPoints;
    private ArrayList<ArrayList<DoublePoint>> keySizes;
    private ArrayList<Double> keyHeight;
    private double leftBorder = 0, rightBorder = 150;

    public Voronoi(){
        optimalPoints = new ArrayList<>();
        keySizes = new ArrayList<>();
        keyHeight = new ArrayList<>();
        for(int i =0 ; i < 4; i++){
            ArrayList<MistakeModel> row = new ArrayList<>();
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
                optimalPoints.get(0).add(entry);
            }else if(i < 19){
                optimalPoints.get(1).add(entry);
            } else if(i < 28){
                optimalPoints.get(2).add(entry);
            } else if(i < 32){
                optimalPoints.get(3).add(entry);
            }

            i++;

            Log.d("MISTAKE", entry.toString());
            Log.d("MISTAKE", key.getX() + "; " + key.getY());
        }

        System.out.println("ROW SIZE: " + optimalPoints.get(3).size());

    }

    public ArrayList<ArrayList<DoublePoint>> calcAdjustments(){
        int i = 0;
        for (ArrayList<MistakeModel> row : optimalPoints) {
            ArrayList<DoublePoint> rowSizes = new ArrayList<>();
            int heightMistakes = 0;
            int numOfHeightMistakes = 0;

            int totalHeightAdjustment = 0;

            for(MistakeModel m : row) {
                if(m.getMistakeY() != 0) {
                    heightMistakes += Math.abs(m.getMistakeX());
                    numOfHeightMistakes++;
                }
            }

            numOfHeightMistakes = (numOfHeightMistakes == 0) ? 1 : numOfHeightMistakes;

            totalHeightAdjustment = heightMistakes / numOfHeightMistakes;

            int rowWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            if(i == 1) {
                rowWidth -= rowWidth * 0.1;
            }
            int originalRowWidth = rowWidth;
            System.out.println("ROW WIDTH:" + rowWidth);

            //Adjusting keys with mistakes
            for (MistakeModel m : row) {
                if(Math.abs(m.getMistakeX()) > 10 && m.getTotalMistakes() >= Voronoi.MIN_NUM_OF_MISTAKES) {
                    double width = m.getKey().width;
                    //Increasing width by percentage of average mistake on X axis in proportion to button width
                    width += width * ((Math.abs(m.getMistakeX()) / width)) * Voronoi.WIDTH_EXPAND_COEF;
                    m.getKey().width = (int)width;
                    rowWidth -= width;
                    m.setAdjusted(true);
                } else if(/*!neighbourHasMistake(m, row) || */isStaticKey(m.getKey().label.toString())) {
                    m.setAdjusted(true);
                    rowWidth -= m.getKey().width;
                }
            }

            int adjustedRowWidth = 0;
            for (MistakeModel m : row) {
                if(m.getTotalMistakes() == 0 && !m.isAdjusted()) {
                    //m.getKey().width = rowWidth / (row.size() - alreadyAdjustedKeys);
                    m.getKey().width -= m.getKey().width * Voronoi.WIDTH_SHRINK_COEF;
                }
                adjustedRowWidth += m.getKey().width;
            }

            //Normalizing to max width if all keys get too large
            int check = 0;
            double coef = originalRowWidth / (double) adjustedRowWidth;

            for (MistakeModel m : row) {
                //adjusting height
                m.getKey().height = m.getKey().height + (int)(totalHeightAdjustment * Voronoi.HEIGHT_EXPAND_COEF);


                //We don't adjust DONE, SPACE, DELETE, CAPS by width
                if(!isStaticKey(m.getKey().label.toString())) {
                    m.getKey().width *= coef;
                }

                //Normalizing adjusted width
                rowSizes.add(new DoublePoint(m.getKey().width, m.getKey().height));
                check += m.getKey().width;
            }

            System.out.println("WIDTHS ORIGINAL: " + originalRowWidth);
            System.out.println("WIDTHS ADJUSTED: " + adjustedRowWidth);
            System.out.println("FINAL WIDTH: " + check);
            keySizes.add(rowSizes);
            i++;
        }
    return keySizes;
    }


    private boolean neighbourHasMistake(MistakeModel m, ArrayList<MistakeModel> row) {
        boolean mistakeLeft = false;
        boolean mistakeRight = false;
        int index = row.indexOf(m);

        if(index > 0) {
            mistakeLeft = row.get(index - 1).getTotalMistakes() > 0;
        }
        if(index < row.size() - 1) {
            mistakeRight = row.get(index + 1).getTotalMistakes() > 0;
        }

        return mistakeLeft || mistakeRight;
    }

    private boolean isStaticKey(String label) {
        return label.equals("SPACE") || label.equals("DEL") || label.equals("CAPS") || label.equals("DONE");
    }


    public ArrayList<ArrayList<DoublePoint>> getKeySizes() {
        return keySizes;
    }
}
