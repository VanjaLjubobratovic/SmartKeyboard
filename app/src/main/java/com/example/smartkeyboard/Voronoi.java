package com.example.smartkeyboard;


import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Voronoi {
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

    public ArrayList<ArrayList<DoublePoint>> calcWidth(){
        int i = 0;
        for (ArrayList<MistakeModel> row : optimalPoints) {
            ArrayList<DoublePoint> rowSizes = new ArrayList<>();
            int rowMistakes = 0;
            int rowWidth = 0;
            int alreadyAdjustedKeys = 0;

            for(MistakeModel m : row) {
                rowMistakes += m.getTotalMistakes();
                //rowWidth += m.getKey().width;
            }

            rowWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            //rowWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            rowWidth -= (row.size() - 1) * 3;
            if(i == 1) {
                rowWidth -= rowWidth * 0.08;
            }
            int originalRowWidth = rowWidth;
            System.out.println("ROW WIDTH:" + rowWidth);

            //Adjusting keys with mistakes
            for (MistakeModel m : row) {
                if(Math.abs(m.getMistakeX()) > 10) {
                    alreadyAdjustedKeys++;
                    //Adjusting key width in proportion to the ammount of mistakes happening per
                    //that key in a row
                    double width = m.getKey().width;
                    //width += width * ((double)m.getTotalMistakes() / (double)rowMistakes);

                    //Increasing width by percentage of average mistake on X axis in proportion to button width
                    width += width * ((Math.abs(m.getMistakeX()) / width)) * 0.7;
                    m.getKey().width = (int)width;
                    rowWidth -= width;
                    m.setAdjusted(true);
                    alreadyAdjustedKeys++;
                } else if(/*!neighbourHasMistake(m, row) || */isStaticKey(m.getKey().label.toString())) {
                    m.setAdjusted(true);
                    rowWidth -= m.getKey().width;
                    alreadyAdjustedKeys++;
                }
            }

            int adjustedRowWidth = 0;
            for (MistakeModel m : row) {
                if(m.getTotalMistakes() == 0 && !m.isAdjusted()) {
                    m.getKey().width = rowWidth / (row.size() - alreadyAdjustedKeys);
                }
                adjustedRowWidth += m.getKey().width;
            }

            adjustedRowWidth -= (row.size() - 1) * 3;
            if(i == 1) {
                adjustedRowWidth -= rowWidth * 0.08;
            }

            //Normalizing to max width if all keys get too large
            for (MistakeModel m : row) {
                double coef = 1;
                if(adjustedRowWidth > originalRowWidth) {
                    coef = originalRowWidth / (double)adjustedRowWidth;
                }
                System.out.println("WIDTHS ORIGINAL: " + originalRowWidth);
                System.out.println("WIDTHS ADJUSTED: " + adjustedRowWidth);
                rowSizes.add(new DoublePoint(m.getKey().width * coef, m.getKey().height));
            }
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
