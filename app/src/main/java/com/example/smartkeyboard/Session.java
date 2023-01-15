package com.example.smartkeyboard;

import android.content.Context;
import android.graphics.Point;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Session implements Parcelable {
    private String user, sessionID, testedKeyboard, nativeKeyboard;
    private int numOfPhrases;
    private long startTime;
    private Orientation orientation;
    private TypingMode typingMode;

    private ArrayList<HashMap<String, String>> transcribed;
    private ArrayList<Integer> time;
    /* Array of key value pairs which will contain all stats
       TER -> Total error rate
       NCER -> Non-corrected errors
       WPM -> words per minute
       AWPM -> adjusted WPM
     */
    private ArrayList<HashMap<String, Double>> stats;
    private ArrayList<ArrayList<Point>> touchPoints;
    private LinkedHashMap<String, MistakeModel> mistakes;

    private Keyboard keyboard;

    protected Session(Parcel in){
        user = in.readString();
        sessionID = in.readString();
        testedKeyboard = in.readString();
        nativeKeyboard = in.readString();
        numOfPhrases = in.readInt();
        startTime = in.readLong();
        typingMode = TypingMode.valueOf(in.readString());
        orientation = Orientation.valueOf(in.readString());
    }

    public Session() {
        this.numOfPhrases = 40;
        this.user = "";
        this.sessionID = "";
        this.testedKeyboard = "";
        this.nativeKeyboard = "";
        this.orientation = Orientation.PORTRAIT;
        this.typingMode = TypingMode.TWO_THUMBS;
        this.clearData();
    }

    public static final Creator<Session> CREATOR = new Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }

        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };

    public ArrayList<ArrayList<Point>> getTouchPoints() {
        return touchPoints;
    }

    public ArrayList<HashMap<String, Double>> getStats() {
        return stats;
    }

    public int getNumOfPhrases() {
        return numOfPhrases;
    }

    public void setNumOfPhrases(int numOfPhrases) {
        this.numOfPhrases = numOfPhrases;
    }

    public ArrayList<HashMap<String, String>> getTranscribed() {
        return transcribed;
    }

    public void setTranscribed(ArrayList<HashMap<String, String>> transcribed) {
        this.transcribed = transcribed;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getTestedKeyboard() {
        return testedKeyboard;
    }

    public void setTestedKeyboard(String testedKeyboard) {
        this.testedKeyboard = testedKeyboard;
    }

    public String getNativeKeyboard() {
        return nativeKeyboard;
    }

    public void setNativeKeyboard(String nativeKeyboard) {
        this.nativeKeyboard = nativeKeyboard;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public TypingMode getTypingMode() {
        return typingMode;
    }

    public void setTypingMode(TypingMode typingMode) {
        this.typingMode = typingMode;
    }

    public void putOriginalPhrase(String phrase) {
        this.transcribed.get(getSize()).put("ORIGINAL", phrase);
    }
    
    public String getStatsString(int ind) {
        HashMap<String, Double> errsMap;

        //Error calculation
        if (ind == -1) {
            errsMap = this.stats.get(getSize() - 1);
        } else if (ind > -1 && ind < this.stats.size()){
            errsMap = this.stats.get(ind);
        } else {
            return "Invalid index";
        }

        if(errsMap != null) {
            return "TER: " + String.format("%.2f", errsMap.get("TER")) + "\n"
                    +  "NCER: " + String.format("%.2f", errsMap.get("NCER")) + "\n"
                    + "WPM: " + String.format("%.2f", errsMap.get("WPM")) + "\n"
                    + "AWPM: " + String.format("%.2f", errsMap.get("AWPM"));
        } else {
            return "errsMap null";
        }

    }

    public void setOrientation (String orientation) {
        switch (orientation) {
            case("PORTRAIT"):
                this.orientation = Orientation.PORTRAIT;
                break;
            case("LANDSCAPE"):
                this.orientation = Orientation.LANDSCAPE;
                break;
        }
    }

    public void setTypingMode (String typingMode) {
        switch(typingMode){
            case("TWO_THUMBS"):
                this.typingMode = TypingMode.TWO_THUMBS;
                break;
            case("ONE_HAND"):
                this.typingMode = TypingMode.ONE_HAND;
                break;
            case("CRADLING"):
                this.typingMode = TypingMode.CRADLING;
                break;
        }
    }

    public String getTime() {
        int last = time.size() - 1;
        return " " + this.time.get(last) / 1000  + "." + this.time.get(last) % 100 + "s";
    }

    public int getSize() {
        if (transcribed.size() == 0) {
            return 0;
        } else {
            return transcribed.size() - 1;
        }
    }

    public void nextPhrase() {
        HashMap<String, String> map = new HashMap<>();
        map.put("RAW", "");
        map.put("FINAL", "");
        map.put("ORIGINAL", "");
        transcribed.add(map);
        touchPoints.add(new ArrayList<>());
    }

    public boolean transcribe(String newInput, String truePhrase) {
        int index = getSize();
        StringBuilder sb = new StringBuilder(transcribed.get(index).get("RAW"));

        //Check if enter is pressed to cycle to next phrase
        if(newInput.length() != 0 && newInput.charAt(newInput.length() - 1) == '\n') {
            timerStop();
            calculateMistakes(truePhrase);
            calculateErrors(truePhrase);
            nextPhrase();
            return false;
        }

        //First letter, timer start
        if(transcribed.get(index).get("RAW").length() == 0) {
            timerStart();
        }


        if (transcribed.get(index).get("FINAL").length() > newInput.length()) {
            sb.append("<");
        } else if (sb.length() == 0) {
            /*This is here because for some reason onTextChanged is triggered
            when returning from session settings*/
            sb.append(newInput);
        } else {
            sb.append(newInput.charAt(newInput.length() - 1));
        }

        //transcribed.set(index, new Pair<>(sb.toString(), newInput));
        HashMap<String, String> newMap = new HashMap<>();
        newMap.put("RAW", sb.toString());
        newMap.put("FINAL", newInput);
        transcribed.set(index, newMap);

        Log.d("TRANSCRIBE", "RAW: " + transcribed.get(index).get("RAW") + " || TRANSCRIBED: " + transcribed.get(index).get("FINAL"));
        return true;
    }

    private void calculateErrors(String truePhrase) {
        String transcribed = this.transcribed.get(getSize()).get("FINAL");

        Log.d("ERRORS", truePhrase);

        double C = 0;
        double INF = 0;
        double IF = 0;

        int m = truePhrase.length();
        int n = transcribed.length();

        int[][] dp = new int[m+1][n+1];

        for(int i = 0; i <= m; i++) {
            for(int j = 0; j <= n; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (truePhrase.charAt(i - 1) == transcribed.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i][j - 1], dp[i - 1][j]), dp[i - 1][j - 1]);
                }
            }
        }

        INF = dp[m][n];

        if(m > n) {
            C = m - INF;
        } else {
            C = n - INF;
        }

        double NCER = (INF / (C + INF + IF)) * 100;
        double TER = ((INF + IF) / (C + INF + IF)) * 100;

        HashMap<String, Double> err = new HashMap<>();
        err.put("NCER", NCER);
        err.put("TER", TER);
        stats.add(err);

        calculateWpm(transcribed, TER, time.get(time.size() - 1));
    }

    private void calculateMistakes(String original) {
        String finalInput = this.transcribed.get(getSize()).get("FINAL");
        int length = 0;

        if (finalInput != null) {
            length = Math.min(original.length(), finalInput.length());
        } else return;

        for(int i = 0; i < length; i++) {
            if (original.toCharArray()[i] != finalInput.toCharArray()[i]) {
                String key = original.substring(i,i+1);
                if(original.charAt(i) == ' ') {
                    key = "SPACE";
                }

                MistakeModel m = mistakes.get(key);
                Point touch = touchPoints.get(touchPoints.size() - 1).get(i);

                if (m != null) {
                    int missX = touch.x - m.getCenterX();
                    int missY = touch.y - m.getCenterY();

                    Log.d("MISTAKE", missX + " " + missY + " || " + m.getKey().width + " " + m.getKey().height);

                    //If press is simply too far from correct letter we ignore such mistake
                    if (Math.abs(missX) > m.getKey().width || Math.abs(missY) > m.getKey().height) {
                        continue;
                    } else if (key.equals("SPACE")) {
                        //If there's a space in original phrase and pressed key is too far from space so it isn't a typo
                        //the keyboard probably didn't accept user's space press, so we mitigate it here
                        //so that correctly typed text after that isn't automatically counted as errors

                        StringBuilder sb = new StringBuilder(finalInput);
                        sb.insert(i, " ");
                        finalInput = sb.toString();
                        length = Math.min(finalInput.length(), original.length());
                        touchPoints.get(touchPoints.size() - 1).add(i,touch);
                    }

                    m.addMistakes(missX, missY);
                } else {
                    Log.e("ERROR", "Key: " + key + " cannot be found in map");
                    Log.e("ERROR", mistakes.keySet().toString());
                }
            }
        }
    }

    public void fillKeyMap(Keyboard keyboard) {
        List<Keyboard.Key> keyList = keyboard.getKeys();
        this.keyboard = keyboard;
        for (Keyboard.Key k : keyList) {
            mistakes.put(k.label.toString(), new MistakeModel(k));
        }
        //logMistakes();
    }

    private void calculateWpm(String phrase, double TER, int time) {
        double WPM = (phrase.length() / 5.0) / ((double) time / 60000.0);
        double accuracy = (100 - TER) / 100.0;
        double AWPM = WPM * accuracy;

        stats.get(stats.size() - 1).put("WPM", WPM);
        stats.get(stats.size() - 1).put("AWPM", AWPM);
    }

    public void addTouchPoint(int x, int y) {
        touchPoints.get(touchPoints.size()-1).add(new Point(x, y));
    }

    public void clearData() {
        this.transcribed = new ArrayList<>();
        this.time = new ArrayList<>();
        this.stats = new ArrayList<>();
        this.touchPoints = new ArrayList<>();
        this.mistakes = new LinkedHashMap<>();
        this.nextPhrase();
    }

    public void timerStart() {
        startTime = SystemClock.elapsedRealtime();
    }

    public void timerStop() {
        long elapsedTime = (SystemClock.elapsedRealtime() - startTime);
        time.add((int) elapsedTime);
    }

    public void logMistakes() {
        for(MistakeModel m : mistakes.values()) {
            Log.d("MISTAKE", m.toString());
            Log.d("CENTROID", m.toStringCentroid());
        }
    }

    public boolean isSet() {
        if (this.user != null
                && this.sessionID != null
                && this.testedKeyboard != null
                && this.nativeKeyboard != null
                && this.orientation != null
                && this.typingMode != null) {
            return true;
        } else {
            //TODO: this back to false after testing
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean isDone() {
        if (this.numOfPhrases == this.getSize()) {
            for(MistakeModel m : mistakes.values()) {
                m.calculateAvgMistake();
                m.calculateCentroid();
            }
            logMistakes();

        }
        return this.numOfPhrases == this.getSize();
    }

    public void resizeKeyboard(Context context) {
        Voronoi v = new Voronoi();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            v.mapToList(mistakes);
        }
        v.calcHeight();
        v.calcWidth();

        ArrayList<DoublePoint> dp = new ArrayList<>();

        for(ArrayList<DoublePoint> l : v.getKeySizes()) {
            dp.addAll(l);
        }

        List<Keyboard.Key> keyList = keyboard.getKeys();

        String filename = "keyboardConfig.txt";
        File file = new File(context.getFilesDir(), filename);
        if(file.delete()) {
            System.out.println("File deleted");
        }

        try {
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < keyList.size(); i++) {
                output.append((int)dp.get(i).getX()).append(";").append((int)dp.get(i).getY()).append(";").append(keyList.get(i).label).append("\n");
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(output.toString());
            bw.close();
            Log.d("FILE WRITER", "pointWriteToCSV: SUCCESS");
        } catch (IOException e) {
            Log.d("FILE WRITER", "pointWriteToCSV: IOException");
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(user);
        parcel.writeString(sessionID);
        parcel.writeString(testedKeyboard);
        parcel.writeString(nativeKeyboard);
        parcel.writeInt(numOfPhrases);
        parcel.writeLong(startTime);
        parcel.writeString(typingMode.name());
        parcel.writeString(orientation.name());
    }
}
