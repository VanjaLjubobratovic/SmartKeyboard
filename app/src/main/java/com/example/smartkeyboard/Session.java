package com.example.smartkeyboard;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Session implements Parcelable {
    private String user, sessionID, testedKeyboard, nativeKeyboard;
    private int numOfPhrases;
    private long startTime;
    private Orientation orientation;
    private TypingMode typingMode;

    protected Session(Parcel in){
        user = in.readString();
        sessionID = in.readString();
        testedKeyboard = in.readString();
        nativeKeyboard = in.readString();
        numOfPhrases = in.readInt();
        startTime = in.readLong();
    }

    //Pair<RawTranscription, FinalTranscription>
    private ArrayList<Pair<String, String>> transcribed;
    private ArrayList<Integer> time;

    public Session() {
        this.transcribed = new ArrayList<>();
        this.time = new ArrayList<>();
        this.numOfPhrases = 40;
        this.nextPhrase();
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

    public int getNumOfPhrases() {
        return numOfPhrases;
    }

    public void setNumOfPhrases(int numOfPhrases) {
        this.numOfPhrases = numOfPhrases;
    }

    public ArrayList<Pair<String, String>> getTranscribed() {
        return transcribed;
    }

    public void setTranscribed(ArrayList<Pair<String, String>> transcribed) {
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

    public String getTime() {
        int last = time.size() - 1;
        return " " + this.time.get(last) / 1000  + ":" + this.time.get(last) % 100 + "s";
    }

    public void addTime(Integer measured) {
        this.time.add(measured);
    }

    public int getSize() {
        if (transcribed.size() == 0) {
            return 0;
        } else {
            return transcribed.size() - 1;
        }
    }

    public void nextPhrase() {
        transcribed.add(new Pair<>("", ""));
    }

    public boolean transcribe(String newInput) {
        int index = getSize();
        StringBuilder sb = new StringBuilder(transcribed.get(index).first);

        if(newInput.length() != 0 && newInput.charAt(newInput.length() - 1) == '\n') {
            nextPhrase();
            timerStop();
            return false;
        }

        if(transcribed.get(index).first.length() == 0) {
            timerStart();
        }

        if (transcribed.get(index).second.length() > newInput.length()) {
            sb.append("-");
        } else if (sb.length() == 0) {
            sb.append(newInput);
        } else {
            sb.append(newInput.charAt(newInput.length() - 1));
        }

        transcribed.set(index, new Pair<>(sb.toString(), newInput));

        Log.d("TRANSCRIBE", "RAW: " + transcribed.get(index).first + " || TRANSCRIBED: " + transcribed.get(index).second);
        return true;
    }

    public void timerStart() {
        startTime = SystemClock.elapsedRealtime();
    }

    public void timerStop() {
        long elapsedTime = (SystemClock.elapsedRealtime() - startTime);
        time.add((int) elapsedTime);
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
    }
}
