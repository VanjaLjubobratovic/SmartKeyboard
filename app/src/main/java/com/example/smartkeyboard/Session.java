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

    //Pair<RawTranscription, FinalTranscription>
    private ArrayList<Pair<String, String>> transcribed;
    private ArrayList<Integer> time;

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

    public TypingMode getTypingMode() {
        return typingMode;
    }

    public void setTypingMode(TypingMode typingMode) {
        this.typingMode = typingMode;
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
        transcribed.add(new Pair<>("", ""));
    }

    public boolean transcribe(String newInput) {
        int index = getSize();
        StringBuilder sb = new StringBuilder(transcribed.get(index).first);

        //Check if enter is pressed to cycle to next phrase
        if(newInput.length() != 0 && newInput.charAt(newInput.length() - 1) == '\n') {
            nextPhrase();
            timerStop();
            return false;
        }

        //First letter, timer start
        if(transcribed.get(index).first.length() == 0) {
            timerStart();
        }


        if (transcribed.get(index).second.length() > newInput.length()) {
            sb.append("<");
        } else if (sb.length() == 0) {
            /*This is here because for some reason onTextChanged is triggered
            when returning from session settings*/
            sb.append(newInput);
        } else {
            sb.append(newInput.charAt(newInput.length() - 1));
        }

        transcribed.set(index, new Pair<>(sb.toString(), newInput));

        Log.d("TRANSCRIBE", "RAW: " + transcribed.get(index).first + " || TRANSCRIBED: " + transcribed.get(index).second);
        return true;
    }

    public void clearData() {
        this.transcribed = new ArrayList<>();
        this.time = new ArrayList<>();
        this.nextPhrase();
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
        parcel.writeString(typingMode.name());
        parcel.writeString(orientation.name());
    }
}
